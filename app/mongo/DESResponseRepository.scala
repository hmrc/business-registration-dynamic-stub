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

package mongo

import javax.inject.Inject

import cats.data.OptionT
import models.SetupDesResponse
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import reactivemongo.play.json.ImplicitBSONHandlers.{BSONDocumentWrites,JsObjectDocumentWriter}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DESResponseRepo @Inject()(implicit val mongo: ReactiveMongoComponent) {
  private lazy val repository = new DESResponseMongoRepository

  def apply(): DESResponseMongoRepository = repository
}

trait DESResponseRepository extends ReactiveRepository[SetupDesResponse, BSONObjectID]{
  def storeNextDesResponse(response: SetupDesResponse): Future[WriteResult]
  def fetchNextDesResponse: OptionT[Future, SetupDesResponse]
  def resetDesResponse: Future[WriteResult]
}

class DESResponseMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SetupDesResponse, BSONObjectID]("setup-des-response", mongo, SetupDesResponse.mongoFormat, ReactiveMongoFormats.objectIdFormats)
    with DESResponseRepository {

  override def storeNextDesResponse(response: SetupDesResponse): Future[WriteResult] = {
    collection
      .update(ordered = true)
      .one(BSONDocument(), response, upsert = true)(global, BSONDocumentWrites, domainFormatImplicit)
  }

  override def fetchNextDesResponse: OptionT[Future, SetupDesResponse] = {
    OptionT(collection.find(BSONDocument(), projection = None)(BSONDocumentWrites, JsObjectDocumentWriter)
      .one[SetupDesResponse](domainFormatImplicit, global))
  }

  override def resetDesResponse: Future[WriteResult] = {
    collection.delete().one(Json.obj())(global,JsObjectDocumentWriter)
  }
}
