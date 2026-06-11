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

package models.hip

import play.api.libs.json.{Json, OFormat, Reads}

final case class BusinessRegistrationRequest(acknowledgementReference: String,
                                             metadata: Option[Metadata],
                                             payAsYouEarnType: Option[PayAsYouEarnType],
                                             registration: Option[Registration]
                                            )

object BusinessRegistrationRequest {
  implicit val format: OFormat[BusinessRegistrationRequest] = Json.format[BusinessRegistrationRequest]
}

final case class Registration(metaData: Metadata, corporationTax: CorporationTax)

object Registration {
  implicit val format: OFormat[Registration] = Json.format[Registration]
}

final case class CorporationTax(companyUTR: Option[String],
                                companyOfficeNumber: String,
                                companyActiveDate: Option[String],
                                hasCompanyTakenOverBusiness: Boolean,
                                companyMemberOfGroup: Boolean,
                                companiesHouseCompanyName: String,
                                companyNameAbbreviation: Option[String],
                                crn: Option[String],
                                startDateOfFirstAccountingPeriod: Option[String],
                                intendedAccountsPreparationDate: Option[String],
                                returnsOnCT61: Boolean,
                                companyACharity: Boolean,
                                companyACharityIncOrg: Option[Boolean],
                                charityTaxpayerReference: Option[String],
                                businessAddress: Address,
                                businessTakeOverDetails: Option[BusinessTakeOverDetails],
                                groupDetails: Option[GroupDetails],
                                businessContactName: Option[Name] ,
                                businessContactDetails: BusinessContactDetails

                               )

object CorporationTax {
  implicit val format: OFormat[CorporationTax] = Json.format[CorporationTax]
}


final case class GroupDetails(
                               parentCompanyName: String,
                               companyGroupName: String,
                               parentUTR: String,
                               groupAddress: Address
                             )

object GroupDetails {
  implicit val format: OFormat[GroupDetails] = Json.format[GroupDetails]
}

final case class BusinessTakeOverDetails(
                                          businessNameLine1: String,
                                          businessNameLine2: Option[String],
                                          businessEntity: Option[String],
                                          businessTakeoverCRN: Option[String],
                                          businessTakeoverAddress: Address,
                                          previousOwnerName: String,
                                          prevOwnerAddress: Address
                                        )

object BusinessTakeOverDetails {
  implicit val format: OFormat[BusinessTakeOverDetails] = Json.format[BusinessTakeOverDetails]
}


final case class Metadata(
                           businessType: String,
                           sessionID: String,
                           credentialID: String,
                           formCreationTimestamp: String,
                           language: String,
                           submissionFromAgent: Boolean,
                           agentDetails: Option[AgentDetails],
                           clientNameOrReference: Option[String],
                           submissionResponseEmail: Option[String],
                           completionCapacity: String,
                           completionCapacityOther: Option[String],
                           optOutOfSAEnrollment: Option[Boolean],
                           declareAccurateAndComplete: Boolean,
                           confirmTermsAndConditions: Option[Boolean]

                         )

object Metadata {
  implicit val format: OFormat[Metadata] = Json.format[Metadata]
}


final case class AgentDetails(
                             name: Name,
                             vrn: Option[String],
                             businessName: Option[String],
                             address: Option[Address],
                             contactDetails: ContactDetails
                             )

object AgentDetails {
  implicit val format: OFormat[AgentDetails] = Json.format[AgentDetails]
}



final case class Name(
                     title: Option[String],
                     firstName: Option[String],
                     middleName: Option[String],
                     lastName: Option[String]
                     )

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}

final case class Address(
                           addressLine1: String,
                           addressLine2: String,
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postcode: Option[String],
                           country: Option[String]
                         )

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}


final case class ContactDetails(
                                 phoneNumber: Option[String],
                                 mobileNumber: Option[String],
                                 faxNumber: Option[String],
                                 email: Option[String]
                               )

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

final case class PayAsYouEarnType(
                                   limitedCompany: LimitedCompany,
                                   employingPeople: EmployingPeople
                                 )

object PayAsYouEarnType {
  implicit val format: OFormat[PayAsYouEarnType] = Json.format[PayAsYouEarnType]
}


final case class EmployingPeople(
                                  dateOfFirstEXBForEmployees: String,

                                  numberOfEmployeesExpectedThisYear: String,
                                  engageSubcontractors: String,
                                  correspondenceName: Option[String],
                                  correspondenceContactDetails: ContactDetails,
                                  payeCorrespondenceAddress: Address

                                )

object EmployingPeople {
  implicit val format: OFormat[EmployingPeople] = Json.format[EmployingPeople]
}


final case class LimitedCompany(
                                 companyUTR: Option[String],
                                 companiesHouseCompanyName: String,
                                 nameOfBusiness: Option[String],
                                 businessAddress: Option[Address],
                                 businessContactDetails: BusinessContactDetails,
                                 natureOfBusiness:  String,
                                 crn: Option[String],
                                 directors: Seq[DirectorDetails],
                                 registeredOfficeAddress: Address,
                                 operatingOccPensionScheme: Boolean
                               )

object LimitedCompany {
  implicit val format: OFormat[LimitedCompany] = Json.format[LimitedCompany]
}
final case class BusinessContactDetails(
                                         phoneNumber: Option[String],
                                         mobileNumber: Option[String],
                                         faxNumber: Option[String],
                                         email: Option[String]
                                       )

object BusinessContactDetails {
  implicit val format: OFormat[BusinessContactDetails] = Json.format[BusinessContactDetails]
}
final case class DirectorDetails(directorName: Name,
                                 directorNINO: Option[String]
                                )

object DirectorDetails {
  implicit val format: OFormat[DirectorDetails] = Json.format[DirectorDetails]
}