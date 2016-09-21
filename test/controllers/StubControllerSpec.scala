/*
 * Copyright 2016 HM Revenue & Customs
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

import org.scalatest.WordSpecLike
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._

class StubControllerSpec extends WordSpecLike with WithFakeApplication with UnitSpec {

  class Setup {
    val controller = new StubController {}
  }

  "Submit" should {
    "return a 202 for basic a JSON body" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson("{}"))
      status(call(controller.show(), request)) shouldBe ACCEPTED
    }
    "return a 202 for a complex JSON body" in new Setup {
      val request = FakeRequest().withJsonBody(Json.toJson("{'test' : 123, 'test2': 'string', 'test3' : {'obj':'obj2', 'testArray':['1','2','3']}}"))
      status(call(controller.show(), request)) shouldBe ACCEPTED
    }
    "return a 400" in new Setup {
      val request = FakeRequest().withBody("""{}""")
      status(call(controller.show(), request)) shouldBe BAD_REQUEST
    }
    "3return a 400" in new Setup {
      val request = FakeRequest().withTextBody("I'm not Json!")
      status(call(controller.show(), request)) shouldBe BAD_REQUEST
    }
  }
}