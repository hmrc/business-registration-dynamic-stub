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

package services

import mocks.MockConfig
import models.{CurlETMPNotification, ETMPNotification}
import mongo.{DESResponseRepository, ETMPNotificationRepository}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

import scala.concurrent.Future

class NotificationServiceSpec extends AnyWordSpec with GuiceOneAppPerSuite with Matchers with MockitoSugar with MockConfig {

  val mockRepo = mock[ETMPNotificationRepository]
  val mockDesRespRepo = mock[DESResponseRepository]
  val mockWs: WSClient = app.injector.instanceOf[WSClient]

  class Setup {

    object TestService extends NotificationService(mockRepo, mockDesRespRepo, mockConfig, mockWs) {

      override val busRegNotif = "/testUrl/"
      override val username = "testUserName"
      override val password = "testPassword"

    }

  }

  "cacheNotification" should {

    val data = CurlETMPNotification(
      "aaa", "bbb", "ccc", Some("ddd"), "eee"
    )

    "return false" in new Setup {
      when(mockRepo.cacheETMPNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(false))

      val result = await(TestService.cacheNotification(data))
      result shouldBe false
    }

    "return true" in new Setup {
      when(mockRepo.cacheETMPNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(true))

      val result = await(TestService.cacheNotification(data))
      result shouldBe true
    }
  }

  "getCachedNotification" should {

    val data = ETMPNotification(
      "aaa", "bbb", Some("ccc"), "ddd"
    )

    "return an optional ETMP notif model" in new Setup {
      when(mockRepo.retrieveETMPNotification(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(data)))

      val result = await(TestService.getCachedNotification("testAcKRef"))
      result shouldBe Some(data)
    }

    "return None" in new Setup {
      when(mockRepo.retrieveETMPNotification(ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      val result = await(TestService.getCachedNotification("testAcKRef"))
      result shouldBe None
    }
  }

  "destroyCachedNotifications" should {
    "return a string" in new Setup {
      when(mockRepo.wipeETMPNotification)
        .thenReturn(Future.successful("Wiped"))

      val result = await(TestService.destroyCachedNotifications)
      result shouldBe "Wiped"
    }
  }
}
