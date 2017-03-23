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

package controllers

import models._
import org.joda.time.DateTime
import org.scalatest.WordSpecLike
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import services.NotificationService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class StubControllerSpec extends WordSpecLike with WithFakeApplication with UnitSpec with MockitoSugar {

  val testDateTime = DateTime.parse("2016-10-10T17:00:00.000Z")

  val mockNotifService = mock[NotificationService]

  implicit val materializer = fakeApplication.materializer

  class Setup {
    val controller = new StubController {
      def dateTime = testDateTime
      val notificationService = mockNotifService
    }
  }

  class SetupNoTimestamp {
    val controller = new StubController {
      def dateTime = testDateTime

      val notificationService = mockNotifService

      override def generateTimestamp: String = {
        dateTime.toString()
      }
    }
  }

  def mockResponse(statusCode : Int) : WSResponse = {
    val m = mock[WSResponse]
    when(m.status).thenReturn(statusCode)
    m
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
          "2016-10-10T17:00:00.000Z",
          submissionFromAgent = false,
          "en",
          "Director",
          Some(""),
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
      val request = FakeRequest().withBody(Json.toJson(fullDesSubmission))
      val result = await(call(controller.submit(), request))
      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe successResponse
    }

    "return a 400 with a reason in json when an invalid des submission fails validation" in new Setup {
      val request = FakeRequest().withBody(Json.toJson("""{"test" : "toFailValidation"}"""))
      val result = await(call(controller.submit(), request))
      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe invalidJsonResponse
    }

    "return a 400 with a reason in json when presented with malformed json" in new Setup {
      import play.api.http.HeaderNames.CONTENT_TYPE
      val request = FakeRequest().withBody("malformed").withHeaders(CONTENT_TYPE -> "application/json")
      val result = await(call(controller.submit(), request))
      status(result) shouldBe BAD_REQUEST
    }
  }

  "generateTimeStamp" should {

    "return a valid UTC timestamp based on the datetime present" in new Setup {
      controller.generateTimestamp shouldBe "2016-10-10T17:00:00.000Z"
    }
  }

  "generateAckRef" should {

    "return a generated acknowledgement reference number" in new Setup {
      controller.generateAckRef shouldBe "SCRS01234567890"
    }
  }

  "cacheNotificationData" should {
    val request = FakeRequest()
      .withBody(Json.obj(
        "ackRef" -> "testAckRef",
        "timestamp" -> "1234567890",
        "regime" -> "testRegime",
        "business-tax-identifier" -> "1234567890",
        "status" -> "04"
      ))

    val data = CurlETMPNotification(
      "testAckRef",
      "1234567890",
      "testRegime",
      Some("1234567890"),
      "04"
    )

    "return an OK" in new Setup {
      when(mockNotifService.cacheNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(false))

      val result = controller.cacheNotificationData()(request)
      status(result) shouldBe OK
    }

    "return an internal server error" in new Setup {
      when(mockNotifService.cacheNotification(ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(true))

      val result = controller.cacheNotificationData()(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "updateCTRecord" should {
    val data = ETMPNotification(
      "1234567890","testRegime",Some("1234567890"),"04"
    )

    val successResponse = mockResponse(OK)

    "return a bad request" in new Setup {
      when(mockNotifService.getCachedNotification(ArgumentMatchers.eq("testAckRef")))
        .thenReturn(Future.successful(None))

      val result = controller.updateCTRecord("testAckRef")(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "return a OK" in new Setup {
      when(mockNotifService.getCachedNotification(ArgumentMatchers.eq("testAckRef")))
        .thenReturn(Future.successful(Some(data)))

      when(mockNotifService.callBRN(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(successResponse))

      val result = controller.updateCTRecord("testAckRef")(FakeRequest())
      status(result) shouldBe OK
    }
  }

  "removeCachedNotifications" should {
    "return an ok" in new Setup {
      when(mockNotifService.destroyCachedNotifications)
        .thenReturn(Future.successful("dropped"))

      val result = controller.removeCachedNotifications()(FakeRequest())
      status(result) shouldBe OK
    }
  }

  "submitPaye" should {
    "return an ok" in new Setup {
      val result = controller.submitPaye()(FakeRequest())
      status(result) shouldBe OK
    }
  }
}
