/*
 * Copyright 2021 HM Revenue & Customs
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

package mongo

import cats.data.OptionT
import javax.inject.{Inject, Singleton}
import models.SetupIVOutcome
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers.BSONDocumentWrites
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IVOutcomeRepository @Inject()(mongo: ReactiveMongoComponent) extends ReactiveRepository[SetupIVOutcome, BSONObjectID](
  "setup-iv-outcome",
  mongo.mongoConnector.db,
  SetupIVOutcome.mongoFormat,
  ReactiveMongoFormats.objectIdFormats) {

  def upsertIVOutcome(data: SetupIVOutcome): Future[WriteResult] = {
    collection
      .update(ordered = true)
      .one(BSONDocument("journeyId" -> data.journeyId), data, upsert = true)(global, BSONDocumentWrites, domainFormatImplicit)
  }

  def fetchIVOutcome(journeyId: String): OptionT[Future, SetupIVOutcome] = {
    OptionT(collection.find(BSONDocument("journeyId" -> journeyId), projection = None).one[SetupIVOutcome])
  }
}
