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

package util

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.DeleteResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait MongoIntegrationSpec extends AnyWordSpec with Matchers {

  implicit class MongoRepositoryOps[T](repo: PlayMongoRepository[T])(implicit ex: ExecutionContext, ct: ClassTag[T]) {
    def count: Int = await(repo.collection.countDocuments().toFuture()).toInt
    def deleteAll: DeleteResult = await(repo.collection.deleteMany(BsonDocument()).toFuture())
    def findAll: Seq[T] = await(repo.collection.find(BsonDocument()).toFuture())
  }

}
