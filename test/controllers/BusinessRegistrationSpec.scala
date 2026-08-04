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

package controllers

import cats.data.OptionT
import mocks.MockConfig
import models.Regime
import models.hip._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.NotificationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessRegistrationSpec extends AnyWordSpec with Matchers with MockitoSugar with MockConfig {
  "submit for CT" should {
    "return a 200 with registration submission for regime CT after successful validation of full model" in new Setup {

      val fullModel = registration.copy(
        metadata = Some(fullMetadata),
        payAsYouEarnType = Some(fullPayAsYouEarn),
        registration = Some(registration.registration.get.copy(
          metaData = fullMetadata,
          corporationTax = fullCorpTax
        ))
      )

      val requestJson = Json.toJson(fullModel)

      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined
    }

    "return a 200 with registration that includes metadata.agentDetails submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(agentDetails = Some(agentDetails)))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      val agentJson = requestJson \ "metadata" \ "agentDetails"
      (agentJson \ "name" \ "title").asOpt[String] shouldBe Some("Mr")
      (agentJson \ "name" \ "firstName").asOpt[String] shouldBe Some("firstName")
      (agentJson \ "name" \ "middleName").asOpt[String] shouldBe Some("middle name")
      (agentJson \ "name" \ "lastName").asOpt[String] shouldBe Some("lastName")
      (agentJson \ "vrn").asOpt[String] shouldBe Some("vrn")
      (agentJson \ "businessName").asOpt[String] shouldBe Some("some business name")
      (agentJson \ "address" \ "addressLine1").asOpt[String] shouldBe Some("address line 1")
      (agentJson \ "address" \ "addressLine2").asOpt[String] shouldBe Some("City")
      (agentJson \ "contactDetails" \ "phoneNumber").asOpt[String] shouldBe Some("5555555")
      (agentJson \ "contactDetails" \ "mobileNumber").asOpt[String] shouldBe Some("11111")
      (agentJson \ "contactDetails" \ "faxNumber").asOpt[String] shouldBe Some("6666666")
      (agentJson \ "contactDetails" \ "email").asOpt[String] shouldBe Some("test@gmail.com")
    }

    "return a 200 with registration that includes metadata.clientNameOrReference submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(clientNameOrReference = Some("clientNameOrRef")))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      (requestJson \ "metadata" \ "clientNameOrReference").asOpt[String].get shouldBe "clientNameOrRef"

    }

    "return a 200 with registration that includes metadata.submissionResponseEmail submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(submissionResponseEmail = Some("test@test.com")))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      (requestJson \ "metadata" \ "submissionResponseEmail").asOpt[String].get shouldBe "test@test.com"

    }


    "return a 200 with registration that includes metadata.completionCapacityOther submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(completionCapacityOther = Some("completion capacity other")))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      (requestJson \ "metadata" \ "completionCapacityOther").asOpt[String].get shouldBe "completion capacity other"

    }


    "return a 200 with registration that includes metadata.optOutOfSAEnrollment submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(optOutOfSAEnrollment = Some(true)))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      (requestJson \ "metadata" \ "optOutOfSAEnrollment").asOpt[Boolean].get shouldBe true

    }

    "return a 200 with registration that includes metadata.confirmTermsAndConditions submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(metadata = Some(metadata.copy(confirmTermsAndConditions = Some(true)))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (responseJson \ "processingDate").asOpt[String] shouldBe defined

      (requestJson \ "metadata" \ "confirmTermsAndConditions").asOpt[Boolean].get shouldBe true

    }


    "return a 200 with registration that includes corporationTax.companyUTR submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(companyUTR = Some("123456789"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "companyUTR").asOpt[String].get shouldBe "123456789"

    }


    "return a 200 with registration that includes corporationTax.companyActiveDate submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(companyActiveDate = Some("2026-10-10"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "companyActiveDate").asOpt[String].get shouldBe "2026-10-10"

    }


    "return a 200 with registration that includes corporationTax.companyNameAbbreviation submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(companyNameAbbreviation = Some("companyNameAbbreviation"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "companyNameAbbreviation").asOpt[String].get shouldBe "companyNameAbbreviation"

    }

    "return a 200 with registration that includes corporationTax.crn submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(crn = Some("12345678"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "crn").asOpt[String].get shouldBe "12345678"

    }


    "return a 200 with registration that includes corporationTax.startDateOfFirstAccountingPeriod submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration =
        Some(registration.registration.get.copy(corporationTax =
          corporationTax.copy(startDateOfFirstAccountingPeriod = Some("2026-10-09"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "startDateOfFirstAccountingPeriod").asOpt[String].get shouldBe "2026-10-09"

    }


    "return a 200 with registration that includes corporationTax.intendedAccountsPreparationDate submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(intendedAccountsPreparationDate = Some("2026-10-09"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "intendedAccountsPreparationDate").asOpt[String].get shouldBe "2026-10-09"

    }

    "return a 200 with registration that includes corporationTax.companyACharityIncOrg submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(companyACharityIncOrg = Some(true))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "companyACharityIncOrg").asOpt[Boolean].get shouldBe true

    }

    "return a 200 with registration that includes corporationTax.charityTaxpayerReference submission for regime CT after successful validation" in new Setup {
      val requestJson = Json.toJson(registration.copy(registration = Some(registration.registration.get.copy(corporationTax = corporationTax.copy(charityTaxpayerReference = Some("ref"))))))
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit("CT"), request)

      status(result) shouldBe OK
      val responseJson = contentAsJson(result) \ "success"
      (responseJson \ "acknowledgementReference").as[String] shouldBe "SCRS01234567890"
      (requestJson \ "registration" \ "corporationTax" \ "charityTaxpayerReference").asOpt[String].get shouldBe "ref"
    }

    "return BadRequest for an invalid payload" in new Setup {
      val requestJson = Json.obj({
        "someKey" -> "someValue"
      })
      val request = FakeRequest().withJsonBody(requestJson)
      val result = call(controller.submit(Regime.CT), request)

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result).as[HipFailureResponse] shouldBe
        HipFailureResponse("Your submission contains one or more errors")
    }
  }

  "submit for PAYE" should {
    "fetch the response" in new Setup {
      val request = FakeRequest().withJsonBody(Json.obj())
      val result = call(controller.submit(Regime.PAYE), request)

      status(result) shouldBe ACCEPTED
    }
  }


  class Setup {
    val mockNotifyService: NotificationService = mock[NotificationService]
    val controller = new BusinessRegistration(mockNotifyService, stubControllerComponents())
    when(mockNotifyService.fetchNextHipResponse).thenReturn(OptionT(Future.successful(None: Option[SetupHipResponse])))
    implicit val system: ActorSystem = ActorSystem("test")
    implicit val mat: Materializer = Materializer(system)

    val metadata = Metadata(
      businessType = "Limited company",
      sessionID = "sessionId",
      credentialID = "credentialId",
      formCreationTimestamp = "2026-06-08",
      language = "ENG",
      submissionFromAgent = true,
      agentDetails = None,
      clientNameOrReference = None,
      submissionResponseEmail = None,
      completionCapacity = "Self-employed",
      completionCapacityOther = None,
      optOutOfSAEnrollment = None,
      declareAccurateAndComplete = true,
      confirmTermsAndConditions = Some(true)
    )

    val address = Address(
      "address line 1",
      "City",
      None,
      None,
      None,
      None
    )

    val fullAddress = address.copy(
      addressLine3 = Some("line 3"),
      addressLine4 = Some("line 4"),
      postcode = Some("AA1 5BB"),
      country = Some("UK")
    )
    val name = Name(
      title = Some("Mr"),
      firstName = Some("firstName"),
      lastName = Some("lastName"),
      middleName = Some("middle name")
    )
    val agentDetails = AgentDetails(
      name = name,
      vrn = Some("vrn"),
      address = Some(address),
      businessName = Some("some business name"),
      contactDetails = ContactDetails(
        phoneNumber = Some("5555555"),
        mobileNumber = Some("11111"),
        faxNumber = Some("6666666"),
        email = Some("test@gmail.com")
      ))

    val fullMetadata = metadata.copy(
      agentDetails = Some(agentDetails),
      clientNameOrReference = Some("clientRef"),
      submissionResponseEmail = Some("response@test.com"),
      completionCapacityOther = Some("other capacity"),
      optOutOfSAEnrollment = Some(true)
    )
    val corporationTax = CorporationTax(
      companyUTR = Some("123456789"),
      companyOfficeNumber = "12345678",
      companyActiveDate = None,
      hasCompanyTakenOverBusiness = false,
      companyMemberOfGroup = false,
      companiesHouseCompanyName = "company name",
      companyNameAbbreviation = None,
      crn = None,
      startDateOfFirstAccountingPeriod = None,
      intendedAccountsPreparationDate = None,
      returnsOnCT61 = true,
      companyACharity = false,
      companyACharityIncOrg = None,
      charityTaxpayerReference = Some("taxPayer ref"),
      businessAddress = address,
      businessTakeOverDetails = None,
      groupDetails = None,
      businessContactName = None,
      businessContactDetails =
        BusinessContactDetails(
          None,
          None,
          None,
          None

        ))
    val fullCorpTax = corporationTax.copy(
      companyActiveDate = Some("2026-01-01"),
      companyNameAbbreviation = Some("CompAbbr"),
      crn = Some("12345678"),
      startDateOfFirstAccountingPeriod = Some("2026-04-06"),
      intendedAccountsPreparationDate = Some("2027-04-05"),
      companyACharityIncOrg = Some(true),
      charityTaxpayerReference = Some("charityRef"),
      businessAddress = fullAddress,
      businessTakeOverDetails = Some(BusinessTakeOverDetails(
        businessNameLine1 = "Takeover Business",
        businessNameLine2 = Some("Takeover Line 2"),
        businessEntity = Some("Partnership"),
        businessTakeoverCRN = Some("TO123456"),
        businessTakeoverAddress = fullAddress,
        previousOwnerName = "Previous Owner",
        prevOwnerAddress = fullAddress
      )),
      groupDetails = Some(GroupDetails(
        parentCompanyName = "Parent Corp",
        companyGroupName = Some("Group Name"),
        parentUTR = Some("9876543210"),
        groupAddress = fullAddress
      )),
      businessContactName = Some(name),
      businessContactDetails = BusinessContactDetails(
        phoneNumber = Some("7777777"),
        mobileNumber = Some("22222"),
        faxNumber = Some("8888888"),
        email = Some("contact@test.com")
      )
    )
    val registration = BusinessRegistrationRequest(
      acknowledgementReference = "SCRS01234567890",
      metadata = Some(metadata),
      payAsYouEarnType = None,
      registration =
        Some(Registration(
          metaData =
            Metadata(
              businessType = "Limited company",
              sessionID = "sessionId",
              credentialID = "credentialId",
              formCreationTimestamp = "2026-06-08",
              language = "ENG",
              submissionFromAgent = true,
              agentDetails = None,

              clientNameOrReference = None,
              submissionResponseEmail = None,


              completionCapacity = "Self-employed",
              completionCapacityOther = None,
              optOutOfSAEnrollment = None,
              declareAccurateAndComplete = true,
              confirmTermsAndConditions = Some(true)


            ),
          corporationTax =
            corporationTax
        )
        )
    )

    val fullPayAsYouEarn = PayAsYouEarnType(
      limitedCompany = Some(LimitedCompany(
        companyUTR = Some("1234567890"),
        companiesHouseCompanyName = "Full Company Name",
        nameOfBusiness = Some("Trading Name"),
        businessAddress = Some(fullAddress),
        businessContactDetails = BusinessContactDetails(
          phoneNumber = Some("5555555"),
          mobileNumber = Some("11111"),
          faxNumber = Some("6666666"),
          email = Some("business@test.com")
        ),
        natureOfBusiness = "Software Development",
        crn = Some("12345678"),
        directors = Seq(DirectorDetails(directorName = name, directorNINO = Some("AB123456C"))),
        registeredOfficeAddress = fullAddress,
        operatingOccPensionScheme = Some(true)
      )),
      employingPeople = EmployingPeople(
        dateOfFirstEXBForEmployees = "2026-01-01",
        numberOfEmployeesExpectedThisYear = "10",
        engageSubcontractors = true,
        correspondenceName = Some("Correspondence Name"),
        correspondenceContactDetails = ContactDetails(
          phoneNumber = Some("9999999"),
          mobileNumber = Some("33333"),
          faxNumber = Some("4444444"),
          email = Some("corr@test.com")
        ),
        payeCorrespondenceAddress = fullAddress
      )
    )
  }
}
