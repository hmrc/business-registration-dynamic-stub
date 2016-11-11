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
package mongo

import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONString}
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ETMPNotificationRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with Eventually with WithFakeApplication {

  class Setup {
    val repository = new ETMPNotificationMongoRepository()
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  def setupCollection(repo: ETMPNotificationRepository, ctRegistration: CurlETMPNotification): Future[WriteResult] = {
    repo.insert(ctRegistration)
  }

  "ackRefSelector" should {
    "return a BSONDoc" in new Setup {
      repository.ackRefSelector("testAckRef") shouldBe BSONDocument("ackRef" -> BSONString("testAckRef"))
    }
  }

  "cacheETMPNotification" should {
    val data = CurlETMPNotification(
      "aaa","bbb","ccc",Some("ddd"),"eee"
    )

    "insert a document" in new Setup {
      val result = repository.cacheETMPNotification(data)

      result.hasErrors shouldBe false
    }
  }

  "retrieveETMPNotification" should {
    val data = CurlETMPNotification(
      "aaa","bbb","ccc",Some("ddd"),"eee"
    )

    "retrieve a document" in new Setup {
      repository.cacheETMPNotification(data)
      val result = repository.retrieveETMPNotification("aaa").get

      result shouldBe CurlETMPNotification.convertToETMPNotification(data)
    }

    "return none" in new Setup {
      val result = await(repository.retrieveETMPNotification("zzz"))

      result shouldBe None
    }
  }

  "wipe" should {
    "return a string" in new Setup {
      val result = await(repository.wipeETMPNotification)
      result shouldBe "Collection dropped"
    }
  }
}