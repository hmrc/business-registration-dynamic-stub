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

import models.{CurlETMPNotification, ETMPNotification}
import mongo.ETMPNotificationRepository
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

class NotificationServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockRepo = mock[ETMPNotificationRepository]

  class Setup {
    object TestService extends NotificationService {
      val etmpRepo = mockRepo
      val busRegNotif = "/testUrl/"

      val username = "testUserName"
      val password = "testPassword"
    }
  }

  def mockWriteResult(fails : Boolean = false) : WriteResult = {
    val m = mock[WriteResult]
    when(m.hasErrors).thenReturn(fails)
    m
  }

  "cacheNotification" should {

    val data = CurlETMPNotification(
      "aaa","bbb","ccc",Some("ddd"),"eee"
    )

    val success = mockWriteResult()
    val fail = mockWriteResult(true)

    "return false" in new Setup {
      when(mockRepo.cacheETMPNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(success))

      val result = await(TestService.cacheNotification(data))
      result shouldBe false
    }

    "return true" in new Setup {
      when(mockRepo.cacheETMPNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(fail))

      val result = await(TestService.cacheNotification(data))
      result shouldBe true
    }
  }

  "getCachedNotification" should {

    val data = ETMPNotification(
      "aaa","bbb",Some("ccc"),"ddd"
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
