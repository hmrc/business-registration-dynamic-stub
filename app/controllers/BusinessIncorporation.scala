/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import cats.instances.FutureInstances
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessIncorporation @Inject()( cc: ControllerComponents)(implicit val ec: ExecutionContext)  extends BackendController(cc) with FutureInstances {

  val logger: Logger = Logger(this.getClass())
  def incorporate(regime: String): Action[AnyContent] = Action { request =>
    logger.info(s"[BusinessIncorporation] [incorporate] Received incorporate for regime ${regime} containing: ${request.body}")
    Accepted(Json.obj("success" -> {Json.obj("processingDate" -> "2015-12-17T09:30:47Z", "acknowledgementReference" -> "SCRS01234567890")}))
  }

}



