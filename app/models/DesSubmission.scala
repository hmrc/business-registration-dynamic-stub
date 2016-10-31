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

package models

import org.joda.time.DateTime


class DesSubmission {


case class BusinessAddress(
                          line1 : String,
                          line2 : String,
                          line3 : Option[String],
                          line4 : Option[String],
                          postcode : Option[String],
                          country : Option[String]
                          )

case class BusinessContactDetails(
                                 phoneNumber : Option[String],
                                 mobileNumber : Option[String],
                                 email : Option[String]
                                 )

case class BusinessContactName(
                              firstName : String,
                              middleNames : Option[String],
                              lastName: Option[String]
                              )

case class CompletionCapacity(
                             completionCapacity : String,
                             completionCapacityOther : Option[String]
                             )

case class Metadata(
                   businessType: String,
                   sessionId: String,
                   credentialId: String,
                   formCreationTimestamp: DateTime,
                   submissionFromAgent: Boolean,
                   language: String,
                   completionCapacity: CompletionCapacity,
                   declareAccurateAndComplete: Boolean
                   )

case class CorporationTax(
                         companyOfficeNumber : String,
                         companyActiveDate: String,
                         hasCompanyTakenOverBusiness: Boolean,
                         companyMemberOfGroup : Boolean,
                         companiesHouseCompanyName : String,
                         crn : String,
                         startDateOfFirstAccountingPeriod : String,
                         intendedAccountsPreparationDate : String,
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

case class FullDesSubmission(
                            acknowledgementReference : String,
                            registration : Registration
                            )



}