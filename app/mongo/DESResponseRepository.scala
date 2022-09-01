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

package mongo

import cats.data.OptionT
import models.SetupDesResponse
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DESResponseRepository @Inject()(mongo: MongoComponent)
                                     (implicit ec: ExecutionContext) extends PlayMongoRepository[SetupDesResponse](
  mongoComponent = mongo,
  collectionName = "setup-des-response",
  domainFormat = SetupDesResponse.mongoFormat,
  indexes = Seq()
) {

  def storeNextDesResponse(response: SetupDesResponse): Future[UpdateResult] =
    collection.replaceOne(BsonDocument(), response, ReplaceOptions().upsert(true)).toFuture()

  def fetchNextDesResponse: OptionT[Future, SetupDesResponse] =
    OptionT(collection.find(BsonDocument()).headOption())

  def resetDesResponse: Future[DeleteResult] =
    collection.deleteOne(BsonDocument()).toFuture()
}
