/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.Inject

import cats.instances.FutureInstances
import config.Config
import models._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.NotificationService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class StubControllerImpl @Inject()(val notificationService : NotificationService,
                                   config: Config) extends StubController {
  def dateTime = DateTime.now(DateTimeZone.UTC)
  val busRegNotification = config.baseUrl("business-registration-notification")
}

trait StubController extends BaseController with FutureInstances {

  def dateTime: DateTime

  val notificationService : NotificationService

  private lazy val malformedJsonResponse = DesFailureResponse("Invalid JSON message received")
  private lazy val invalidJsonResponse = DesFailureResponse("Your submission contains one or more errors")
  private lazy val successDesResponse = DesSuccessResponse(generateTimestamp, generateAckRef)

  val submit: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
      Try(request.body.validate[FullDesSubmission]) match {
        case Success(JsSuccess(desSubmission, _)) =>
        fetchDesResponse {
            Logger.info(s"[DES Submission] [Success] - $desSubmission")
            Ok(Json.toJson(successDesResponse))
          }
        case Success(JsError(errors)) =>
          Logger.warn("Errors from submission" + errors)
          Future.successful(BadRequest(Json.toJson(invalidJsonResponse)))
        case Failure(e) =>
          Logger.warn("Exception thrown" + e)
          Future.successful(BadRequest(Json.toJson(malformedJsonResponse)))
      }
  }

  def setupNextDESResponse(status: Int): Action[AnyContent] = Action.async(BodyParsers.parse.anyContent) {
    implicit request =>
      notificationService.setupNextDESResponse(status, request.body.asJson) map (_ => Ok)
  }

  def cacheNotificationData : Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      withJsonBody[CurlETMPNotification] {
        etmp =>
          notificationService.cacheNotification(etmp) map {
            case false => Ok
            case true => InternalServerError
          }
      }
  }

  def notifyBRN(ackRef : String) : Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.getCachedNotification(ackRef) flatMap {
        case Some(record) =>
          notificationService.callBRN(ackRef, record) map {
            resp => new Status(resp.status)
          }
        case None =>
          Future.successful(BadRequest("Could not find notification"))
      }
  }

  def removeCachedNotifications() : Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.destroyCachedNotifications map {
        resp => Ok(Json.obj("status" -> resp))
      }
  }

  private[controllers] def generateTimestamp : String = {
    val dT = ISODateTimeFormat.dateTime()
    dT.print(dateTime)
  }

  private[controllers] def generateAckRef: String = "SCRS01234567890"

  private[controllers] def fetchDesResponse(default: => Result): Future[Result] = notificationService.fetchNextDesResponse.semiflatMap { response =>
      notificationService.resetDesResponse.map { _ =>
        Status(response.status)(Json.toJson(response)(SetupDesResponse.responseWrites))
      }
    }.getOrElse{
      default
    }

  val submitPaye = Action.async {
    implicit request =>
      fetchDesResponse(Accepted)
  }

  val submitVat = Action.async {
    implicit request =>
      fetchDesResponse(Accepted)
  }

  val topup = Action {
    implicit request =>
      Logger.info(s"[StubController] [topup] Received topup containing: ${request.body}")
      Accepted(Json.obj("processingDate" -> "2015-12-17T09:30:47Z", "acknowledgementReference" -> "SCRS01234567890"))
  }
}
