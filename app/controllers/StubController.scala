/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.instances.FutureInstances
import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import services.NotificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class StubController @Inject()(notificationService: NotificationService,
                               config: ServicesConfig,
                               cc: ControllerComponents) extends BackendController(cc) with FutureInstances {
  def dateTime = LocalDateTime.now(ZoneOffset.UTC)

  val busRegNotification = config.baseUrl("business-registration-notification")

  val logger: Logger = Logger(this.getClass())

  private lazy val malformedJsonResponse = DesFailureResponse("Invalid JSON message received")
  private lazy val invalidJsonResponse = DesFailureResponse("Your submission contains one or more errors")
  private lazy val successDesResponse = DesSuccessResponse(generateTimestamp, generateAckRef)

  val submit: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      Try(request.body.validate[FullDesSubmission]) match {
        case Success(JsSuccess(desSubmission, _)) =>
          fetchDesResponse {
            logger.info(s"[DES Submission] [Success] - $desSubmission")
            Ok(Json.toJson(successDesResponse))
          }
        case Success(JsError(errors)) =>
          logger.warn("Errors from submission" + errors)
          Future.successful(BadRequest(Json.toJson(invalidJsonResponse)))
        case Failure(e) =>
          logger.warn("Exception thrown" + e)
          Future.successful(BadRequest(Json.toJson(malformedJsonResponse)))
      }
  }

  def setupNextDESResponse(status: Int): Action[AnyContent] = Action.async(parse.anyContent) {
    implicit request =>
      notificationService.setupNextDESResponse(status, request.body.asJson) map (_ => Ok)
  }

  def cacheNotificationData: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      withJsonBody[CurlETMPNotification] {
        etmp =>
          notificationService.cacheNotification(etmp) map {
            case false => Ok
            case true => InternalServerError
          }
      }
  }

  def notifyBRN(ackRef: String): Action[AnyContent] = Action.async {
    notificationService.getCachedNotification(ackRef) flatMap {
      case Some(record) =>
        notificationService.callBRN(ackRef, record) map {
          resp => new Status(resp.status)
        }
      case None =>
        Future.successful(BadRequest("Could not find notification"))
    }
  }

  def removeCachedNotifications(): Action[AnyContent] = Action.async {
    notificationService.destroyCachedNotifications map {
      resp => Ok(Json.obj("status" -> resp))
    }
  }

  private[controllers] def generateTimestamp: String = {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateTime.format(formatter)
  }

  private[controllers] def generateAckRef: String = "SCRS01234567890"

  private[controllers] def fetchDesResponse(default: => Result): Future[Result] = notificationService.fetchNextDesResponse.semiflatMap { response =>
    notificationService.resetDesResponse.map { _ =>
      Status(response.status)(Json.toJson(response)(SetupDesResponse.responseWrites))
    }
  }.getOrElse {
    default
  }

  val submitPaye = Action.async {
    fetchDesResponse(Accepted)
  }

  val submitVat = Action.async {
    fetchDesResponse(Accepted)
  }

  val topup = Action { request =>
    logger.info(s"[StubController] [topup] Received topup containing: ${request.body}")
    Accepted(Json.obj("processingDate" -> "2015-12-17T09:30:47Z", "acknowledgementReference" -> "SCRS01234567890"))
  }
}
