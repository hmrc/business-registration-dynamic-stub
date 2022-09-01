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

package mongo

import models._
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.MongoComponent
import util.{IntegrationSpecBase, MongoIntegrationSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ETMPNotificationRepositorySpec extends IntegrationSpecBase with MongoIntegrationSpec with BeforeAndAfterEach with ScalaFutures with Eventually {

  class Setup {

    val rmc = app.injector.instanceOf[MongoComponent]
    val repository = new ETMPNotificationRepository(rmc)

    repository.deleteAll
    repository.count shouldBe 0
    await(repository.ensureIndexes)
  }

  def setupCollection(repo: ETMPNotificationRepository, ctRegistration: CurlETMPNotification): Future[InsertOneResult] =
    repo.collection.insertOne(ctRegistration).toFuture()

  "cacheETMPNotification" should {
    val data = CurlETMPNotification(
      "aaa", "bbb", "ccc", Some("ddd"), "eee"
    )

    "insert a document" in new Setup {
      val result = repository.cacheETMPNotification(data)

      await(result) shouldBe false
    }
  }

  "retrieveETMPNotification" should {
    val data = CurlETMPNotification(
      "aaa", "bbb", "ccc", Some("ddd"), "eee"
    )

    "retrieve a document" in new Setup {
      await(repository.cacheETMPNotification(data))
      val result = await(repository.retrieveETMPNotification("aaa")).get

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
      result shouldBe "All records removed"
    }
  }
}
