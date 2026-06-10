/*
 * Copyright 2026 HM Revenue & Customs
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
import com.google.inject.Singleton
import models.Regime
import models.hip.{BusinessRegistrationRequest, HipFailureResponse, HipSuccessResponse, SetupHipResponse}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.NotificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessRegistration @Inject()(notificationService: NotificationService, cc: ControllerComponents)(implicit val ec: ExecutionContext)
  extends BackendController(cc) with FutureInstances {

  val logger: Logger = Logger(this.getClass())

  def dateTime = LocalDateTime.now(ZoneOffset.UTC)

  private def successHipResponse = HipSuccessResponse(generateTimestamp, generateAckRef)
  private lazy val invalidJsonResponse = HipFailureResponse("Your submission contains one or more errors")


  def setupNextHIPResponse(status: Int): Action[AnyContent] = Action.async(parse.anyContent) {
    implicit request =>
      notificationService.setupNextHIPResponse(status, request.body.asJson) map (_ => Ok)
  }

  def submit(regime: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      regime match {
        case Regime.CT =>
          request.body.validate[BusinessRegistrationRequest] match {
            case JsSuccess(hipSubmission, _) =>
              fetchHipResponse {
                logger.info(s"[HIP Submission] [Success] - $hipSubmission")
                Ok(Json.toJson(successHipResponse))
              }
            case JsError(errors) =>
              logger.warn("Errors from submission" + errors)
              Future.successful(BadRequest(Json.toJson(invalidJsonResponse)))
          }
        case Regime.PAYE =>
          fetchHipResponse(Accepted)
        case _ =>
          Future.successful(BadRequest("regime is Invalid"))
      }
  }

  private[controllers] def fetchHipResponse(default: => Result): Future[Result] = notificationService.fetchNextHipResponse.semiflatMap { response =>
    notificationService.resetHipResponse.map { _ =>
      Status(response.status)(Json.toJson(response)(SetupHipResponse.responseWrites))
    }
  }.getOrElse {
    default
  }


  private[controllers] def generateTimestamp: String = {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateTime.format(formatter)
  }

  private[controllers] def generateAckRef: String = "SCRS01234567890"

}
