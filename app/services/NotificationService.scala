/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.ws.{WS, WSAuthScheme, WSResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import play.api.Play.current
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object NotificationService extends NotificationService {
  val etmpRepo = ETMPNotificationRepository()
  val desResponseRepository: DESResponseRepository = DESResponseRepository()
  val busRegNotif = s"${baseUrl("business-registration-notification")}/business-registration-notification"
  val username = getString(s"$env.basicAuth.username")
  val password = getString(s"$env.basicAuth.password")
}

trait NotificationService extends ServicesConfig {

  val etmpRepo : ETMPNotificationRepository
  val desResponseRepository: DESResponseRepository

  val busRegNotif : String

  val username : String
  val password : String

  def cacheNotification(curl : CurlETMPNotification) : Future[Boolean] = {
    etmpRepo.cacheETMPNotification(curl) map {
      _.hasErrors
    }
  }

  def getCachedNotification(ackRef : String) : Future[Option[ETMPNotification]] = {
    etmpRepo.retrieveETMPNotification(ackRef)
  }

  def setupNextDESResponse(status: Int, optJson: Option[JsValue]): Future[WriteResult] = {
    val desResponse = SetupDesResponse(status, optJson)
    desResponseRepository.storeNextDesResponse(desResponse)
  }

  def fetchNextDesResponse: OptionT[Future, SetupDesResponse] = desResponseRepository.fetchNextDesResponse

  def resetDesResponse: Future[Boolean] = desResponseRepository.resetDesResponse.map(_.ok)

  // $COVERAGE-OFF$
  def callBRN(ackRef : String, eTMPNotification: ETMPNotification) : Future[WSResponse] = {
    val json = Json.toJson(eTMPNotification)
    WS.url(s"$busRegNotif/notification/$ackRef")
      .withAuth(username, password, WSAuthScheme.BASIC)
      .withBody(json)
      .post(json)
  }
  // $COVERAGE-ON$

  def destroyCachedNotifications : Future[String] = etmpRepo.wipeETMPNotification
}
