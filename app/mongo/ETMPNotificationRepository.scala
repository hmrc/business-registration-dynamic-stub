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

import models.{CurlETMPNotification, ETMPNotification}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import reactivemongo.play.json.ImplicitBSONHandlers.BSONDocumentWrites
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ETMPNotificationRepository @Inject()(mongo: ReactiveMongoComponent) extends ReactiveRepository[CurlETMPNotification, BSONObjectID](
  "etmp-notif-store",
  mongo.mongoConnector.db,
  CurlETMPNotification.format,
  ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("ackRef" -> IndexType.Ascending),
      name = Some("AckRefIndex"),
      unique = false,
      sparse = false
    )
  )

  def ackRefSelector(ackRef: String): BSONDocument = {
    BSONDocument("ackRef" -> BSONString(ackRef))
  }

  def cacheETMPNotification(notification: CurlETMPNotification): Future[Boolean] = {
    collection.insert(ordered = true).one(notification) map {
      _ => false
    } recover {
      case _ => true
    }
  }

  def retrieveETMPNotification(ackRef: String): Future[Option[ETMPNotification]] = {
    collection.find(ackRefSelector(ackRef), projection = None).one[CurlETMPNotification] map {
      case Some(record) => Some(CurlETMPNotification.convertToETMPNotification(record))
      case None => None
    }
  }

  def wipeETMPNotification: Future[String] = {
    collection.drop(failIfNotFound = false) map {
      _ => "Collection dropped"
    }
  }
}
