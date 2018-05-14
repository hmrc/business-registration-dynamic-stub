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

package helpers

import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.Results.EmptyContent

import scala.concurrent.Future

trait APIHelper {
  expects: OneServerPerSuite =>

  val ws: WSClient = app.injector.instanceOf[WSClient]

  private def client(path: String): WSRequest = ws.url(s"http://localhost:$port/$path").withFollowRedirects(false)

  def wsPost(path: String, body: Option[JsValue] = None): Future[WSResponse] = {
    body.fold(client(path).post(EmptyContent()))(json => client(path).post(json))
  }

  def wsGet(path: String): Future[WSResponse] = client(path).get
}
