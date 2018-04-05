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

import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox

object DynamicMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Unit] = {
    import c.universe._

    val result: c.universe.Tree = {
      annottees.map(_.tree).toList match {
        case tree :: Nil => q"""println($tree)"""
      }
    }
    c.Expr[Unit](result)
  }
}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class DynamicStub extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DynamicMacro.impl
}
