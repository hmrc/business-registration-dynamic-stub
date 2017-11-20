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

package mongo

import cats.data.OptionT
import models.SetupIVOutcome
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object IVOutcomeRepository extends MongoDbConnection {
  private lazy val repository = new IVOutcomeMongoRepository

  def apply(): IVOutcomeMongoRepository = repository
}

trait IVOutcomeRepository extends Repository[SetupIVOutcome, BSONObjectID]{
  def upsertIVOutcome(data: SetupIVOutcome): Future[WriteResult]
  def fetchIVOutcome(journeyId: String): OptionT[Future, SetupIVOutcome]
}

class IVOutcomeMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SetupIVOutcome, BSONObjectID]("setup-iv-outcome", mongo, SetupIVOutcome.mongoFormat, ReactiveMongoFormats.objectIdFormats)
    with IVOutcomeRepository {

  override def upsertIVOutcome(data: SetupIVOutcome): Future[WriteResult] = {
    collection.update(BSONDocument("journeyId" -> BSONString(data.journeyId)), data, upsert = true)(BSONDocumentWrites, domainFormatImplicit, global)
  }

  override def fetchIVOutcome(journeyId: String): OptionT[Future, SetupIVOutcome] = {
    OptionT(collection.find(BSONDocument("journeyId" -> journeyId)).one[SetupIVOutcome])
  }
}