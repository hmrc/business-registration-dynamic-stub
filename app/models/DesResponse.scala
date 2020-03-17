/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

case class DesFailureResponse(reason: String)

object DesFailureResponse {
  implicit val formats = Json.format[DesFailureResponse]
}

case class DesSuccessResponse(processingDate: String, acknowledgementReference: String)

object DesSuccessResponse {
  implicit val formats = Json.format[DesSuccessResponse]
}

case class SetupDesResponse(status: Int, responseJson: Option[JsValue])

object SetupDesResponse {

  val mongoFormat: OFormat[SetupDesResponse] = Json.format[SetupDesResponse]

  implicit def formatToOFormat(format: Format[SetupDesResponse]): OFormat[SetupDesResponse] = format.asInstanceOf[OFormat[SetupDesResponse]]

  val responseWrites: Writes[SetupDesResponse] = (
    (__ \ "status").write[Int] and
    (__ \ "responseJson").writeNullable[JsValue]
  )(unlift(SetupDesResponse.unapply))
}
