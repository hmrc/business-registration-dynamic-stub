/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json

case class ETMPNotification(timestamp: String,
                            regime: String,
                            `business-tax-identifier`: Option[String],
                            status: String)

object ETMPNotification {
  implicit val format = Json.format[ETMPNotification]
}

case class CurlETMPNotification(ackRef: String,
                                timestamp: String,
                                regime: String,
                                `business-tax-identifier`: Option[String],
                                status: String)

object CurlETMPNotification {
  implicit val format = Json.format[CurlETMPNotification]

  def convertToETMPNotification(curl: CurlETMPNotification): ETMPNotification = {
    ETMPNotification(curl.timestamp, curl.regime, curl.`business-tax-identifier`, curl.status)
  }
}
