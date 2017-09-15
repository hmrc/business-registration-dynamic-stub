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
import models._
import mongo.DESResponseMongoRepository
import org.joda.time.DateTime
import play.api.Application
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.modules.reactivemongo.MongoDbConnection
import util.{IntegrationSpecBase, MongoIntegrationSpec}

import scala.concurrent.ExecutionContext

class SetupDesResponseISpec extends IntegrationSpecBase with MongoIntegrationSpec with APIHelper {

  implicit val ex: ExecutionContext = implicitly[Application].actorSystem.dispatcher.prepare()

  class Setup extends MongoDbConnection {
    val desResponseRepo = new DESResponseMongoRepository()(db)

    desResponseRepo.awaitDrop
  }

  val submissionPath = "corporation-tax"

  val desSubmissionJson: JsValue = Json.parse(
    """
      |{
      |     "acknowledgementReference" : "ack-ref",
      |     "registration" : {
      |       "metadata" : {
      |         "sessionId" : "testBusinessType",
      |         "credentialId" : "testSessionId",
      |         "businessType" : "testCredId",
      |         "formCreationTimestamp" : "2016-10-10T17:00:00.000Z",
      |         "submissionFromAgent" : false,
      |         "language" : "en",
      |         "completionCapacity" : "Director",
      |         "completionCapacityOther" : "",
      |         "declareAccurateAndComplete" : true
      |       },
      |       "corporationTax" : {
      |         "companyOfficeNumber" : "0123456789",
      |         "companyActiveDate" : "2017-09-15T14:28:36.606+01:00",
      |         "hasCompanyTakenOverBusiness" : false,
      |         "companyMemberOfGroup" : false,
      |         "companiesHouseCompanyName" : "testCompanyName",
      |         "crn" : "crn-0123456789",
      |         "startDateOfFirstAccountingPeriod" : "01/01/1980",
      |         "intendedAccountsPreparationDate" : "10/10/1980",
      |         "returnsOnCT61" : true,
      |         "companyACharity" : false,
      |         "businessContactDetails" : {
      |           "phoneNumber" : "1234567890"
      |         }
      |       }
      |     }
      |   }
    """.stripMargin)

  "POST /setup-next-des-response" should {

    val uri = "setup-next-des-response"

    "setup a des response status for the next call to /corporation-tax and once called reset the setup response" in new Setup {

      val setupResponseStatus = 999
      val path = s"$uri/$setupResponseStatus"

      val response: WSResponse = wsPost(path)

      desResponseRepo.awaitCount shouldBe 1
      response.status shouldBe 200

      val res :: Nil = await(desResponseRepo.findAll())
      val expected = SetupDesResponse(setupResponseStatus, None)

      res shouldBe expected

      val submissionResponse: WSResponse = wsPost(submissionPath, Some(desSubmissionJson))

      submissionResponse.status shouldBe setupResponseStatus

      desResponseRepo.awaitCount shouldBe 0

      val submissionResponse2: WSResponse = wsPost(submissionPath, Some(desSubmissionJson))

      submissionResponse2.status shouldBe 200
    }
  }
}
