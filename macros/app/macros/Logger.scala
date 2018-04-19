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
import scala.reflect.macros.whitebox

object Logger {
  def debug(message: String): Unit = macro LoggerImpl.debug
  def info(message: String): Unit = macro LoggerImpl.info
  def warn(message: String): Unit = macro LoggerImpl.warn
  def error(message: String): Unit = macro LoggerImpl.error
  def error(message: String, exception: => Throwable): Unit = macro LoggerImpl.errorWithException
}

class LoggerImpl(val c: whitebox.Context) {
  import c.universe._

  private def buildLog(message: Expr[String]): Expr[String] = {

    def getClassSymbol(s: Symbol): Symbol = if (s.isClass) s else getClassSymbol(s.owner)

    def getMethodSymbol(symbol: Symbol): Symbol = symbol match {
      case s if s.isMethod && s.asMethod.name != "$anonfun" =>
        println("enclosing method " + s.asMethod.name)
        s
      case s if s.isClass =>
        println(s"${s.name.toString} is not enclosed by a method")
        NoSymbol
      case s if s.isTerm =>
        println("enclosing term (val?)" + s.asTerm.name)
        getMethodSymbol(s.owner)
      case s =>
        println("unknown " + s.toString)
        NoSymbol
    }

    val enclosingOwner = getClassSymbol(c.internal.enclosingOwner).toString.stripPrefix("trait").trim
    val enclosingMethod = getMethodSymbol(c.internal.enclosingOwner)
    val enclosingMethodString = enclosingMethod.toString.stripPrefix("method").trim

    c.Expr[String](
      q""" "[" + $enclosingOwner + "] " + ${if (enclosingMethod == NoSymbol) "" else "[" + enclosingMethodString + "] "} + $message""")
  }

  def debug(message: Expr[String]): Expr[Unit] = {
    c.Expr[Unit](q"""play.api.Logger.debug(${buildLog(message)})""")
  }

  def info(message: Expr[String]): Expr[Unit] = {
    c.Expr[Unit](q"""play.api.Logger.info(${buildLog(message)})""")
  }

  def warn(message: Expr[String]): Expr[Unit] = {
    c.Expr[Unit](q"""play.api.Logger.warn(${buildLog(message)})""")
  }

  def error(message: Expr[String]): Expr[Unit] = {
    c.Expr[Unit](q"""play.api.Logger.error(${buildLog(message)})""")
  }

  def errorWithException(message: Expr[String], exception: Tree): Expr[Unit] = {
    c.Expr[Unit](q"""play.api.Logger.error(${buildLog(message)}, $exception)""")
  }
}
