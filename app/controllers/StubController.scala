/*
 * Copyright 2016 HM Revenue & Customs
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

import models.{CurlETMPNotification, DesFailureResponse, DesSuccessResponse, FullDesSubmission}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.NotificationService
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object StubController extends StubController {
  def dateTime = DateTime.now(DateTimeZone.UTC)

  val notificationService = NotificationService

  val busRegNotification = baseUrl("business-registration-notification")
}

trait StubController extends BaseController with ServicesConfig {

  def dateTime: DateTime

  val notificationService : NotificationService

  private lazy val malformedJsonResponse = DesFailureResponse("Invalid JSON message received")
  private lazy val invalidJsonResponse = DesFailureResponse("Your submission contains one or more errors")
  private lazy val successDesResponse = DesSuccessResponse(generateTimestamp, generateAckRef)

  val submit: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
      Try(request.body.validate[FullDesSubmission]) match {
        case Success(JsSuccess(_, _)) => Future.successful(Ok(Json.toJson(successDesResponse)))
        case Success(JsError(errors)) => Future.successful(BadRequest(Json.toJson(invalidJsonResponse)))
        case Failure(e) => Future.successful(BadRequest(Json.toJson(malformedJsonResponse)))
      }
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

  def updateCTRecord(ackRef : String) : Action[AnyContent] = Action.async {
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
}
