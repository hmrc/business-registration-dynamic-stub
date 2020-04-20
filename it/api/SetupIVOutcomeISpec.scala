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

package api

import helpers.APIHelper
import models.SetupIVOutcome
import mongo.IVOutcomeRepository
import play.api.Application
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import util.{IntegrationSpecBase, MongoIntegrationSpec}

import scala.concurrent.ExecutionContext

class SetupIVOutcomeISpec extends IntegrationSpecBase with MongoIntegrationSpec with APIHelper {

  implicit val ex: ExecutionContext = implicitly[Application].actorSystem.dispatcher.prepare()

  class Setup {
    val rmc = app.injector.instanceOf[ReactiveMongoComponent]
    val ivOutcomeRepo = new IVOutcomeRepository(rmc)

    await(ivOutcomeRepo.drop)
    await(ivOutcomeRepo.count) shouldBe 0
  }

  "POST /setup-iv-outcome" should {
    "setup an IV Outcome for the given journeyId" in new Setup {
      val journeyId = "12345"
      val outcome = "Success"
      val expected = SetupIVOutcome(journeyId, outcome)

      val response: WSResponse = await(wsPost(s"identity-verification/setup-iv-outcome/$journeyId/$outcome"))

      ivOutcomeRepo.awaitCount shouldBe 1
      response.status shouldBe 200

      val res :: Nil = await(ivOutcomeRepo.findAll())
      res shouldBe expected
    }
  }

  "GET /mdtp/journey/journeyId/:journeyId" should {
    "return IV Outcome for a given journeyId" in new Setup {
      val journeyId = "123"
      val expectedJson = Json.parse(
        """
          | {
          |   "result": "FailedIV",
          |   "token": "aaaa-bbbb-ccccc"
          | }
        """.stripMargin)

      val data = SetupIVOutcome(journeyId, "FailedIV")
      await(ivOutcomeRepo.upsertIVOutcome(data))

      ivOutcomeRepo.awaitCount shouldBe 1

      val response: WSResponse = await(wsGet(s"identity-verification/mdtp/journey/journeyId/$journeyId"))

      response.status shouldBe 200
      Json.parse(response.body) shouldBe expectedJson
    }
  }
}
