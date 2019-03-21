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

package config

import com.google.inject.AbstractModule
import controllers.{IVStubController, IVStubControllerImpl, StubController, StubControllerImpl}
import services.{IVService, IVServiceImpl, NotificationService, NotificationServiceImpl}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bindControllers()
    bindServices()
    bindConfig()
  }

  def bindControllers(): Unit = {
    bind(classOf[IVStubController]).to(classOf[IVStubControllerImpl]).asEagerSingleton()
    bind(classOf[StubController]).to(classOf[StubControllerImpl]).asEagerSingleton()
  }

  def bindServices(): Unit = {
    bind(classOf[IVService]).to(classOf[IVServiceImpl]).asEagerSingleton()
    bind(classOf[NotificationService]).to(classOf[NotificationServiceImpl]).asEagerSingleton()
  }

  def bindConfig(): Unit = {
    bind(classOf[Config]).to(classOf[ConfigImpl]).asEagerSingleton()
  }
}
