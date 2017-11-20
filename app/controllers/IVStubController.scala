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

package controllers

import cats.instances.FutureInstances
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.IVService
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object IVStubController extends BaseController with ServicesConfig with FutureInstances {

  def ivOutcome(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      IVService.fetchIVOutcome(journeyId).semiflatMap { iv =>
        Future.successful(Ok(Json.obj(
          "result" -> iv.outcome,
          "token" -> "aaaa-bbbb-ccccc"
        )))
      }.getOrElse(NotFound)
  }

  def setupIVOutcome(journeyId: String, outcome: String): Action[AnyContent] = Action.async {
    implicit request =>
      IVService.setupIVOutcome(journeyId, outcome) map (_ => Ok)
  }
}
