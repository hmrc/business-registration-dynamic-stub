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

package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import cats.data.OptionT
import com.mongodb.client.result.UpdateResult
import mocks.MockConfig
import models._
import org.bson.types.ObjectId
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mongodb.scala.bson.BsonString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.mvc.{AnyContentAsJson, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.NotificationService

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class StubControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with MockConfig {

  implicit val system = ActorSystem("test")

  implicit val mat: Materializer = Materializer(system)

  val testDateTime = LocalDateTime.of(2016,10,10,17,1,2,987654321)

  val mockNotifService = mock[NotificationService]

  lazy val controllerComponents = stubControllerComponents(playBodyParsers = stubPlayBodyParsers(mat))

  class Setup {
    val controller = new StubController(mockNotifService, mockConfig, controllerComponents) {
      override def dateTime = testDateTime
    }
  }

  class SetupNoTimestamp {
    val controller = new StubController(mockNotifService, mockConfig, controllerComponents) {
      override def dateTime = testDateTime
    }
  }

  def mockResponse(statusCode: Int): WSResponse = {
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
          "2016-10-10T17:01:02.987Z",
          submissionFromAgent = false,
          "en",
          "Director",
          Some(""),
          declareAccurateAndComplete = true
        ),
        CorporationTax(
          companyOfficeNumber = "0123456789",
          companyActiveDate = Some(testDateTime.toString),
          hasCompanyTakenOverBusiness = false,
          companyMemberOfGroup = false,
          groupDetails = None,
          businessTakeOverDetails = None,
          "testCompanyName",
          crn = Some("crn-0123456789"),
          startDateOfFirstAccountingPeriod = Some("01/01/1980"),
          intendedAccountsPreparationDate = Some("10/10/1980"),
          returnsOnCT61 = true,
          companyACharity = false,
          None,
          None,
          BusinessContactDetails(phoneNumber = Some("1234567890"), None, None)
        )
      )
    )

    val successResponse = Json.toJson(DesSuccessResponse(testDateTime.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'")), ackRef))
    val invalidJsonResponse = Json.toJson(DesFailureResponse("Your submission contains one or more errors"))

    "return a 200 with a timestamp and ack ref if the des submission is validated successfully" in new SetupNoTimestamp {

      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT(Future.successful(None: Option[SetupDesResponse])))

      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission))
      val result = call(controller.submit(), request)
      status(result) shouldBe OK
      contentAsJson(result) shouldBe successResponse
    }

    "return 200 with a successful response with takeover details provided" in new Setup {
      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT(Future.successful(None: Option[SetupDesResponse])))
      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission.copy(
        registration = fullDesSubmission.registration.copy(
          corporationTax = fullDesSubmission.registration.corporationTax.copy(
            hasCompanyTakenOverBusiness = true,
            businessTakeOverDetails = Some(TakeoverDetails(
              businessNameLine1 = "businessNameLine1",
              businessNameLine2 = Some("businessNameLine2"),
              businessEntity = Some("Business Entity"),
              businessTakeoverCRN = Some("C1234567"),
              businessTakeoverAddress = BusinessAddress("line1", "line2", None, None, None, None),
              prevOwnersName = "Joe Bloggs",
              prevOwnerAddress = BusinessAddress("line1", "line2", None, None, None, None)
            ))
          )
        )
      )))
      val result = call(controller.submit(), request)
      status(result) shouldBe OK
      contentAsJson(result) shouldBe successResponse

    }

    "return a 400 if businessTakeoverDetails is true and businessTakeoverDetails is not provided" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission.copy(
        registration = fullDesSubmission.registration.copy(
          corporationTax = fullDesSubmission.registration.corporationTax.copy(hasCompanyTakenOverBusiness = true))
      )))
      val result = call(controller.submit(), request)

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe invalidJsonResponse
    }

    "return 200 with a successful response with group details provided" in new Setup {
      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT(Future.successful(None: Option[SetupDesResponse])))
      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission.copy(
        registration = fullDesSubmission.registration.copy(
          corporationTax = fullDesSubmission.registration.corporationTax.copy(
            companyMemberOfGroup = true,
            groupDetails = Some(GroupDetails("fooBar", None, Some("1234567890"), BusinessAddress("1", "2", None, None, None, None)))))
      )))
      val result = call(controller.submit(), request)
      status(result) shouldBe OK
      contentAsJson(result) shouldBe successResponse

    }

    "return a 400 if companyMemberOfGroup is true and groupDetails is not provided" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson(fullDesSubmission.copy(
        registration = fullDesSubmission.registration.copy(
          corporationTax = fullDesSubmission.registration.corporationTax.copy(companyMemberOfGroup = true))
      )))
      val result = call(controller.submit(), request)
      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe invalidJsonResponse
    }

    "return a 400 with a reason in json when an invalid des submission fails validation" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson("""{"test" : "toFailValidation"}"""))
      val result = call(controller.submit(), request)
      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe invalidJsonResponse
    }

    "return a 400 with a reason in json when presented with malformed json" in new Setup {

      import play.api.http.HeaderNames.CONTENT_TYPE

      val request = FakeRequest().withBody("malformed").withHeaders(CONTENT_TYPE -> "application/json")
      val result = call(controller.submit(), request)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "generateTimeStamp" should {

    "return a valid UTC timestamp based on the datetime present" in new Setup {
      controller.generateTimestamp shouldBe "2016-10-10T17:01:02.987Z"
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
      "1234567890", "testRegime", Some("1234567890"), "04"
    )

    val successResponse = mockResponse(OK)

    "return a bad request" in new Setup {
      when(mockNotifService.getCachedNotification(ArgumentMatchers.eq("testAckRef")))
        .thenReturn(Future.successful(None))

      val result = controller.notifyBRN("testAckRef")(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "return a OK" in new Setup {
      when(mockNotifService.getCachedNotification(ArgumentMatchers.eq("testAckRef")))
        .thenReturn(Future.successful(Some(data)))

      when(mockNotifService.callBRN(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq(data)))
        .thenReturn(Future.successful(successResponse))

      val result = controller.notifyBRN("testAckRef")(FakeRequest())
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
    "return an accepted" in new Setup {
      val result = controller.submitPaye()(FakeRequest())
      status(result) shouldBe ACCEPTED
    }
    "return a 400 if status next des response is 400" in new Setup {
      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT[Future, SetupDesResponse](Future.successful(Some(SetupDesResponse(400, None)))))

      when(mockNotifService.resetDesResponse)
        .thenReturn(Future.successful(true))

      val result = controller.submitPaye()(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }
  }

  "submitVat" should {
    "return the default response (accepted) if a DES response has been setup" in new Setup {
      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT(Future.successful(None: Option[SetupDesResponse])))

      val result = controller.submitVat()(FakeRequest())
      status(result) shouldBe ACCEPTED
    }

    "return a bad request if a DES response has been setup to return a bad request" in new Setup {
      when(mockNotifService.fetchNextDesResponse)
        .thenReturn(OptionT[Future, SetupDesResponse](Future.successful(Some(SetupDesResponse(BAD_REQUEST, None)))))

      when(mockNotifService.resetDesResponse)
        .thenReturn(Future.successful(true))

      val result = controller.submitPaye()(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }
  }

  "topup" should {
    "return an accepted" in new Setup {
      val result = controller.topup()(FakeRequest())
      status(result) shouldBe ACCEPTED
    }
  }

  "setupNextDESResponse" should {

    val writeResult = UpdateResult.acknowledged(1, 0, BsonString(ObjectId.get().toString))

    "not parse the request body if nothing was supplied and return an ok" in new Setup {
      when(mockNotifService.setupNextDESResponse(ArgumentMatchers.eq(BAD_GATEWAY), ArgumentMatchers.eq(None)))
        .thenReturn(Future.successful(writeResult))

      val result: Future[Result] = call(controller.setupNextDESResponse(BAD_GATEWAY), FakeRequest())
      status(result) shouldBe OK
    }

    "parse the request body if it was supplied and return an ok" in new Setup {
      val json: JsObject = Json.obj("test" -> "json")

      when(mockNotifService.setupNextDESResponse(ArgumentMatchers.eq(BAD_GATEWAY), ArgumentMatchers.eq(json.asOpt[JsValue])))
        .thenReturn(Future.successful(writeResult))

      val requestWithJson: FakeRequest[AnyContentAsJson] =
        FakeRequest().withJsonBody(json).withHeaders("Content-Type" -> "application/json")

      val result: Future[Result] = call(controller.setupNextDESResponse(BAD_GATEWAY), requestWithJson)

      status(result) shouldBe OK
    }
  }
}
