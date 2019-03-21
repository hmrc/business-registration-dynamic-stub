/*
 * Copyright 2019 HM Revenue & Customs
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

import models.{CurlETMPNotification, ETMPNotification}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONString}
import reactivemongo.play.json.ImplicitBSONHandlers.BSONDocumentWrites
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ETMPNotificationRepo @Inject()(implicit val mongo: ReactiveMongoComponent) {
  private lazy val repository = new ETMPNotificationMongoRepository

  def apply() : ETMPNotificationMongoRepository = repository
}

class ETMPNotificationMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[CurlETMPNotification, BSONObjectID]("etmp-notif-store", mongo, CurlETMPNotification.format, ReactiveMongoFormats.objectIdFormats) {

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq("ackRef" -> IndexType.Ascending),
      name = Some("AckRefIndex"),
      unique = false,
      sparse = false
    )
  )

  def ackRefSelector(ackRef : String) : BSONDocument = {
    BSONDocument("ackRef" -> BSONString(ackRef))
  }

   def cacheETMPNotification(notification: CurlETMPNotification): Future[Boolean] = {
    collection.insert[CurlETMPNotification](notification) map {
      case wr => false
    } recover {
      case _ => true
    }
  }

   def retrieveETMPNotification(ackRef: String): Future[Option[ETMPNotification]] = {
    collection.find(ackRefSelector(ackRef)).one[CurlETMPNotification] map {
      case Some(record) => Some(CurlETMPNotification.convertToETMPNotification(record))
      case None => None
    }
  }

   def wipeETMPNotification : Future[String] = {
    collection.drop() map {
      _ => "Collection dropped"
    }
  }
}
