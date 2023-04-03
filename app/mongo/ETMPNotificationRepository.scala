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

import models.{CurlETMPNotification, ETMPNotification}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ETMPNotificationRepository @Inject()(mongo: MongoComponent)
                                          (implicit ec: ExecutionContext) extends PlayMongoRepository[CurlETMPNotification](
  mongoComponent = mongo,
  collectionName = "etmp-notif-store",
  domainFormat = CurlETMPNotification.format,
  indexes = Seq(
    IndexModel(
      ascending("ackRef"),
      IndexOptions()
        .name("AckRefIndex")
        .unique(false)
        .sparse(false)
    )
  )
) {

  def cacheETMPNotification(notification: CurlETMPNotification): Future[Boolean] =
    collection.insertOne(notification).toFuture() map { _ => false } recover { case _ => true }

  def retrieveETMPNotification(ackRef: String): Future[Option[ETMPNotification]] =
    collection.find(equal("ackRef", ackRef)).headOption() map {
      case Some(record) => Some(CurlETMPNotification.convertToETMPNotification(record))
      case None => None
    }

  def wipeETMPNotification: Future[String] =
    collection.deleteMany(BsonDocument()).toFuture() map { _ => "All records removed" }
}
