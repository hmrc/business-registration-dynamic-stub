/*
 * Copyright 2026 HM Revenue & Customs
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

package test.api

import models.hip.SetupHipResponse
import mongo.HIPResponseRepository
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import test.util.{IntegrationSpecBase, MongoIntegrationSpec}
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext.Implicits.global

class SetupHipResponseISpec extends IntegrationSpecBase with MongoIntegrationSpec {

  class Setup {
    val rmc = app.injector.instanceOf[MongoComponent]
    val hipResponseRepo = new HIPResponseRepository(rmc)

    hipResponseRepo.deleteAll
    hipResponseRepo.count shouldBe 0
  }

  val submissionPath = "RESTAdapter/business-registration/CT"

  val hipSubmissionJson: JsValue = Json.parse(
    """
      |{
      |  "acknowledgementReference" : "SCRS01234567890",
      |  "metadata" : {
      |    "businessType" : "Limited company",
      |    "sessionID" : "sessionId",
      |    "credentialID" : "credentialId",
      |    "formCreationTimestamp" : "2026-06-08",
      |    "language" : "ENG",
      |    "submissionFromAgent" : true,
      |    "agentDetails" : {
      |      "name" : {
      |        "title" : "Mr",
      |        "firstName" : "firstName",
      |        "middleName" : "middle name",
      |        "lastName" : "lastName"
      |      },
      |      "vrn" : "vrn",
      |      "businessName" : "some business name",
      |      "address" : {
      |        "addressLine1" : "address line 1",
      |        "addressLine2" : "City"
      |      },
      |      "contactDetails" : {
      |        "phoneNumber" : "5555555",
      |        "mobileNumber" : "11111",
      |        "faxNumber" : "6666666",
      |        "email" : "test@gmail.com"
      |      }
      |    },
      |    "completionCapacity" : "Self-employed",
      |    "declareAccurateAndComplete" : true,
      |    "confirmTermsAndConditions" : true
      |  },
      |  "registration" : {
      |    "metaData" : {
      |      "businessType" : "Limited company",
      |      "sessionID" : "sessionId",
      |      "credentialID" : "credentialId",
      |      "formCreationTimestamp" : "2026-06-08",
      |      "language" : "ENG",
      |      "submissionFromAgent" : true,
      |      "completionCapacity" : "Self-employed",
      |      "declareAccurateAndComplete" : true,
      |      "confirmTermsAndConditions" : true
      |    },
      |    "corporationTax" : {
      |      "companyUTR" : "123456789",
      |      "companyOfficeNumber" : "12345678",
      |      "hasCompanyTakenOverBusiness" : false,
      |      "companyMemberOfGroup" : false,
      |      "companiesHouseCompanyName" : "company name",
      |      "returnsOnCT61" : true,
      |      "companyACharity" : false,
      |      "charityTaxpayerReference" : "taxPayer ref",
      |      "businessAddress" : {
      |        "addressLine1" : "address line 1",
      |        "addressLine2" : "City"
      |      },
      |      "businessContactDetails" : { }
      |    }
      |  }
      |}
    """.stripMargin)

  "POST /setup-next-hip-response" should {

    val uri = "RESTAdapter/setup-next-hip-response"

    "setup a hip response status for the next call to /business-registration/CT and once called reset the setup response" in new Setup {

      val setupResponseStatus = 999
      val path = s"$uri/$setupResponseStatus"

      val response: WSResponse = await(wsPost(path))

      hipResponseRepo.count shouldBe 1
      response.status shouldBe 200

      val res = hipResponseRepo.findAll
      val expected = Seq(SetupHipResponse(setupResponseStatus, None))

      res shouldBe expected

      val submissionResponse: WSResponse = await(wsPost(submissionPath, Some(hipSubmissionJson)))

      submissionResponse.status shouldBe setupResponseStatus

      hipResponseRepo.count shouldBe 0

      val submissionResponse2: WSResponse = await(wsPost(submissionPath, Some(hipSubmissionJson)))

      submissionResponse2.status shouldBe 200
    }
  }
}