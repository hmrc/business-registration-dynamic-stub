/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import cats.data.OptionT
import models.{CurlETMPNotification, ETMPNotification, SetupDesResponse}
import mongo.{DESResponseRepository, ETMPNotificationRepository}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class NotificationService @Inject()(etmpRepository: ETMPNotificationRepository,
                                    DESResponseRepository: DESResponseRepository,
                                    config: ServicesConfig,
                                    runMode: RunMode,
                                    val ws: WSClient) {


  val busRegNotif = s"${config.baseUrl("business-registration-notification")}/business-registration-notification"
  val username = config.getString(s"${runMode.env}.basicAuth.username")
  val password = config.getString(s"${runMode.env}.basicAuth.password")

  def cacheNotification(curl: CurlETMPNotification): Future[Boolean] = {
    etmpRepository.cacheETMPNotification(curl)
  }

  def getCachedNotification(ackRef: String): Future[Option[ETMPNotification]] = {
    etmpRepository.retrieveETMPNotification(ackRef)
  }

  def setupNextDESResponse(status: Int, optJson: Option[JsValue]): Future[WriteResult] = {
    val desResponse = SetupDesResponse(status, optJson)
    DESResponseRepository.storeNextDesResponse(desResponse)
  }

  def fetchNextDesResponse: OptionT[Future, SetupDesResponse] = DESResponseRepository.fetchNextDesResponse

  def resetDesResponse: Future[Boolean] = DESResponseRepository.resetDesResponse.map(_.ok)

  // $COVERAGE-OFF$
  def callBRN(ackRef: String, eTMPNotification: ETMPNotification): Future[WSResponse] = {
    val json = Json.toJson(eTMPNotification)
    ws.url(s"$busRegNotif/notification/$ackRef")
      .withAuth(username, password, WSAuthScheme.BASIC)
      .withBody(json)
      .post(json)
  }

  // $COVERAGE-ON$

  def destroyCachedNotifications: Future[String] = etmpRepository.wipeETMPNotification
}
