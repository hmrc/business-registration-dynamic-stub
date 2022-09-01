
import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playVersion                   =  "-play-28"
  private val bootstrapPlayVersion          =  "7.1.0"
  private val scalaTestVersion              =  "3.2.12"
  private val scalaTestPlusPlayVersion      =  "5.1.0"
  private val flexmarkVersion               =  "0.62.2"
  private val wireMockVersion               =  "2.31.0"
  private val hmrcMongoVersion              =  "0.71.0"
  private val catsVersion                   =  "2.7.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo$playVersion"          % hmrcMongoVersion,
    "uk.gov.hmrc"               %% s"bootstrap-backend$playVersion"   % bootstrapPlayVersion,
    "org.typelevel"             %%  "cats-core"                       % catsVersion
  )

  val test = Seq(
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion"     %   hmrcMongoVersion          % "test, it",
    "org.scalatest"             %%  "scalatest"                       %   scalaTestVersion          % "test, it",
    "org.scalatestplus.play"    %%  "scalatestplus-play"              %   scalaTestPlusPlayVersion  % "test, it",
    "com.typesafe.play"         %%  "play-test"                       %   PlayVersion.current       % "test, it",
    "com.vladsch.flexmark"      %   "flexmark-all"                    %   flexmarkVersion           % "test, it",
    "org.scalatestplus"         %%  "mockito-4-5"                     % s"$scalaTestVersion.0"      % "test",
    "com.github.tomakehurst"    %   "wiremock-jre8-standalone"        %   wireMockVersion           % "it"
  )

  def apply() = compile ++ test
}


