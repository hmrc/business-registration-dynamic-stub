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

package models.hip

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

case class HipResponse(success: HipSuccessResponse)
case class HipSuccessResponse(processingDate: String, acknowledgementReference: String)

object HipSuccessResponse {
  implicit val format: OFormat[HipSuccessResponse] = Json.format[HipSuccessResponse]
}

object HipResponse {
  implicit val format: OFormat[HipResponse] = Json.format[HipResponse]
}

case class HipFailureResponse(reason: String)

object HipFailureResponse {
  implicit val format: OFormat[HipFailureResponse] = Json.format[HipFailureResponse]

}

case class SetupHipResponse(status: Int, responseJson: Option[JsValue])

object SetupHipResponse {

  val mongoFormat: OFormat[SetupHipResponse] = Json.format[SetupHipResponse]

  implicit def formatToOFormat(format: Format[SetupHipResponse]): OFormat[SetupHipResponse] = format.asInstanceOf[OFormat[SetupHipResponse]]

  val responseWrites: Writes[SetupHipResponse] = (
    (__ \ "status").write[Int] and
      (__ \ "responseJson").writeNullable[JsValue]
    )(unlift(SetupHipResponse.unapply))
}
