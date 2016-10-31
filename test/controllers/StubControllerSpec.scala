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

package controllers

import models._
import org.joda.time.DateTime
import org.scalatest.WordSpecLike
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._

class StubControllerSpec extends WordSpecLike with WithFakeApplication with UnitSpec {

  val testDateTime = DateTime.parse("2016-10-10T17:00:00.000Z")

  class Setup {
    val controller = new StubController {
      def dateTime = testDateTime
    }
  }

  class SetupNoTimestamp {
    val controller = new StubController {
      def dateTime = testDateTime

      override def generateTimestamp: String = {
        dateTime.toString()
      }
    }
  }

  "Submit" should {

    val ackRef = "SCRS01234567890"

    val fullDesSubmission = FullDesSubmission(
     ackRef,
      Registration(
        Metadata(
          "testBusinessType",
          "testSessionId",
          "testCredId",
          testDateTime,
          submissionFromAgent = false,
          "en",
          CompletionCapacity(
            "Director",
            None
          ),
          declareAccurateAndComplete = true
        ),
        CorporationTax(
          companyOfficeNumber = "0123456789",
          companyActiveDate = testDateTime.toString,
          hasCompanyTakenOverBusiness = false,
          companyMemberOfGroup = false,
          "testCompanyName",
          crn = "crn-0123456789",
          startDateOfFirstAccountingPeriod = "01/01/1980",
          intendedAccountsPreparationDate = "10/10/1980",
          returnsOnCT61 = true,
          companyACharity = false,
          None,
          None,
          BusinessContactDetails(phoneNumber = Some("1234567890"), None, None)
        )
      )
    )

    val successResponse = Json.toJson(DesSuccessResponse(testDateTime.toString, ackRef))
    val invalidJsonResponse = Json.toJson(DesFailureResponse("Your submission contains one or more errors"))
    val malformedJsonResponse = Json.toJson(DesFailureResponse("Invalid JSON message received"))

    "return a 200 with a timestamp and ack ref if the des submission is validated successfully" in new SetupNoTimestamp {
      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission))
      val result = await(call(controller.submit(), request))
      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe successResponse
    }

    "return a 400 with a reason in json when an invalid des submission fails validation" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson("""{"test" : "toFailValidation"}"""))
      val result = await(call(controller.submit(), request))
      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe invalidJsonResponse
    }

    "return a 400 with a reason in json when presented with malformed json" in new Setup {
      val request = FakeRequest().withBody("malformed")
      val result = await(call(controller.submit(), request))
      status(result) shouldBe BAD_REQUEST
      //todo SCRS-2298 - doesn't parse as json so unreachable - any way around it?
      //jsonBodyOf(result).toString() shouldBe malformedJsonResponse
    }
  }

  "generateTimeStamp" should {

    "return a valid UTC timestamp based on the datetime present" in new Setup {
      controller.generateTimestamp shouldBe "2016-10-10T18:00:00+01:00"
    }
  }
}