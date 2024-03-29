/*
 * Copyright 2023 HM Revenue & Customs
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
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.result.UpdateResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IVOutcomeRepository @Inject()(mongo: MongoComponent)
                                   (implicit ec: ExecutionContext) extends PlayMongoRepository[SetupIVOutcome](
  mongoComponent = mongo,
  collectionName = "setup-iv-outcome",
  domainFormat = SetupIVOutcome.mongoFormat,
  indexes = Seq()
) {

  def upsertIVOutcome(data: SetupIVOutcome): Future[UpdateResult] =
    collection.replaceOne(equal("journeyId", data.journeyId), data, ReplaceOptions().upsert(true)).toFuture()


  def fetchIVOutcome(journeyId: String): OptionT[Future, SetupIVOutcome] =
    OptionT(collection.find(equal("journeyId", journeyId)).headOption())
}
