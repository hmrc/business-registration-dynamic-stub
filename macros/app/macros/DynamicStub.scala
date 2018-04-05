/*
 * Copyright 2018 HM Revenue & Customs
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

package macros

import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class DynamicStub extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DynamicStub.impl
}

object DynamicStub {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val result: c.universe.Tree = {
      annottees.map(_.tree).toList match {
        case q"$mods val $methodName: $returnType = Action.async { $request => ..$body }" :: Nil =>
          q"""$mods val $methodName: $returnType = Action.async { $request =>
             println(${methodName.toString()})
             ..$body
          }"""
        case _ => c.abort(c.enclosingPosition, "Annotation @DynamicStub can be used only with Action.async")
      }
    }
    c.Expr[Any](result)
  }
}
