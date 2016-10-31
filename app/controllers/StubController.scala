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

import java.text.SimpleDateFormat
import java.util.Date

import models.{DesFailureResponse, DesSuccessResponse}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object StubController extends StubController {
  def dateTime = DateTime.now(DateTimeZone.UTC)
}

trait StubController extends BaseController with ServicesConfig {

  def dateTime: DateTime

  private lazy val malformedJsonResponse = DesFailureResponse("Invalid JSON message received")
  private lazy val invalidJsonResponse = DesFailureResponse("Your submission contains one or more errors")
  private lazy val successDesResponse = DesSuccessResponse(generateTimestamp, generateAckRef)

  val show: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      Try(request.body.validate[FullDesSubmission]) match {
        case Success(JsSuccess(_, _)) => Future.successful(Ok(Json.toJson(successDesResponse)))
        case Success(JsError(_)) => Future.successful(BadRequest(Json.toJson(invalidJsonResponse)))
        case Failure(e) => Future.successful(BadRequest(Json.toJson(malformedJsonResponse)))
      }
  }

  private def generateTimestamp : String = {
    val timeStampFormat = "yyyy-MM-dd'T'HH:mm:ssXXX"
    val format: SimpleDateFormat = new SimpleDateFormat(timeStampFormat)
    format.format(new Date(dateTime.getMillis))
  }

  private def generateAckRef: String = "SCRS01234567890"
}
