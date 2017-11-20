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
import models.SetupDesResponse
import play.api.libs.json.Json
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object DESResponseRepository extends MongoDbConnection {
  private lazy val repository = new DESResponseMongoRepository

  def apply(): DESResponseMongoRepository = repository
}

trait DESResponseRepository extends Repository[SetupDesResponse, BSONObjectID]{
  def storeNextDesResponse(response: SetupDesResponse): Future[WriteResult]
  def fetchNextDesResponse: OptionT[Future, SetupDesResponse]
  def resetDesResponse: Future[WriteResult]
}

class DESResponseMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SetupDesResponse, BSONObjectID]("setup-des-response", mongo, SetupDesResponse.mongoFormat, ReactiveMongoFormats.objectIdFormats)
    with DESResponseRepository {

  override def storeNextDesResponse(response: SetupDesResponse): Future[WriteResult] = {
    collection.update(BSONDocument(), response, upsert = true)(BSONDocumentWrites, domainFormatImplicit, global)
  }

  override def fetchNextDesResponse: OptionT[Future, SetupDesResponse] = {
    OptionT(collection.find(BSONDocument()).one[SetupDesResponse])
  }

  override def resetDesResponse: Future[WriteResult] = {
    collection.remove(Json.obj())
  }
}
