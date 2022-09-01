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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.JsValue
import play.api.libs.ws.{EmptyBody, WSClient, WSRequest, WSResponse}

import scala.concurrent.Future

trait IntegrationSpecBase extends AnyWordSpec with Matchers with GuiceOneServerPerSuite {

  val ws: WSClient = app.injector.instanceOf[WSClient]

  private def client(path: String): WSRequest = ws.url(s"http://localhost:$port/$path").withFollowRedirects(false)

  def wsPost(path: String, body: Option[JsValue] = None): Future[WSResponse] = {
    body.fold(client(path).post(EmptyBody))(json => client(path).post(json))
  }

  def wsGet(path: String): Future[WSResponse] = client(path).get
}