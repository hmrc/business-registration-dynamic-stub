/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import javax.inject.Inject

import cats.data.OptionT
import models.SetupIVOutcome
import mongo.{IVOutcomeRepo, IVOutcomeRepository}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

class IVServiceImpl @Inject()(val ivOutcomeRepo: IVOutcomeRepo) extends IVService {
  override val ivOutcomeRepository: IVOutcomeRepository = ivOutcomeRepo()
}

trait IVService {
  val ivOutcomeRepository: IVOutcomeRepository

  def setupIVOutcome(journeyId: String, outcome: String): Future[WriteResult] = {
    val desResponse = SetupIVOutcome(journeyId, outcome)
    ivOutcomeRepository.upsertIVOutcome(desResponse)
  }

  def fetchIVOutcome(journeyId: String): OptionT[Future, SetupIVOutcome] = ivOutcomeRepository.fetchIVOutcome(journeyId)
}
