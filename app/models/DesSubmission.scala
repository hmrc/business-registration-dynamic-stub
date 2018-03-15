/*
 * Copyright 2018 HM Revenue & Customs
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

package models

import org.joda.time.DateTime
import play.api.libs.json.{Format, JsPath, Json}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Writes._


case class BusinessAddress(
                          line1 : String,
                          line2 : String,
                          line3 : Option[String],
                          line4 : Option[String],
                          postcode : Option[String],
                          country : Option[String]
                          )

//todo SCRS-2298 - need to make sure at least one of the following exists?
case class BusinessContactDetails(phoneNumber : Option[String],
                                  mobileNumber : Option[String],
                                  email : Option[String])

case class BusinessContactName(
                              firstName : String,
                              middleNames : Option[String],
                              lastName: Option[String]
                              )

case class Metadata(
                   businessType: String,
                   sessionId: String,
                   credentialId: String,
                   formCreationTimestamp: String,
                   submissionFromAgent: Boolean,
                   language: String,
                   completionCapacity : String,
                   completionCapacityOther : Option[String],
                   declareAccurateAndComplete: Boolean
                   )

case class CorporationTax(
                         companyOfficeNumber : String,
                         companyActiveDate: Option[String],
                         hasCompanyTakenOverBusiness: Boolean,
                         companyMemberOfGroup : Boolean,
                         companiesHouseCompanyName : String,
                         crn : Option[String],
                         startDateOfFirstAccountingPeriod : Option[String],
                         intendedAccountsPreparationDate : Option[String],
                         returnsOnCT61 : Boolean,
                         companyACharity : Boolean,
                         businessAddress : Option[BusinessAddress],
                         businessContactName : Option[BusinessContactName],
                         businessContactDetails : BusinessContactDetails
                        )

case class Registration(
                       metadata: Metadata,
                       corporationTax: CorporationTax
                       )

case class FullDesSubmission(acknowledgementReference : String,
                             registration : Registration)

object FullDesSubmission {

  implicit val bcdReads = Json.format[BusinessContactDetails]
  implicit val bcnReads = Json.format[BusinessContactName]
  implicit val baReads = Json.format[BusinessAddress]
  implicit val cTReads = Json.format[CorporationTax]
  implicit val metadataReads: Format[Metadata] = (
    (__ \ "sessionId").format[String] and
    (__ \ "credentialId").format[String] and
    (__ \ "businessType").format[String] and
    (__ \ "formCreationTimestamp").format[String] and
    (__ \ "submissionFromAgent").format[Boolean] and
    (__ \ "language").format[String] and
    (__ \ "completionCapacity").format[String] and
    (__ \ "completionCapacityOther").formatNullable[String] and
    (__ \ "declareAccurateAndComplete").format[Boolean]
    )(Metadata.apply , unlift(Metadata.unapply))
  implicit val registrationReads = Json.format[Registration]
  implicit val fullReads = Json.format[FullDesSubmission]
}
