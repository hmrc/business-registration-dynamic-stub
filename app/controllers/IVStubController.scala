/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject

import cats.instances.FutureInstances
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.IVService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IVStubControllerImpl @Inject()(val iVService: IVService) extends IVStubController

trait IVStubController extends BaseController with FutureInstances {

  val iVService: IVService

  def ivOutcome(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      iVService.fetchIVOutcome(journeyId).semiflatMap { iv =>
        Future.successful(Ok(Json.obj(
          "result" -> iv.outcome,
          "token" -> "aaaa-bbbb-ccccc"
        )))
      }.getOrElse(NotFound)
  }

  def setupIVOutcome(journeyId: String, outcome: String): Action[AnyContent] = Action.async {
    implicit request =>
      iVService.setupIVOutcome(journeyId, outcome) map (_ => Ok)
  }
}
