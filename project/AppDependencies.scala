
import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playVersion                   =  "-play-30"
  private val bootstrapPlayVersion          =  "8.6.0"
  private val scalaTestVersion              =  "3.2.12"
  private val scalaTestPlusPlayVersion      =  "7.0.1"
  private val flexmarkVersion               =  "0.64.8"
  private val wireMockVersion               =  "3.5.4"
  private val hmrcMongoVersion              =  "1.9.0"
  private val catsVersion                   =  "2.10.0"
  private val playTestVersion               =  "3.0.3"

  val compile = Seq(
    ws,
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo$playVersion"          % hmrcMongoVersion,
    "uk.gov.hmrc"               %% s"bootstrap-backend$playVersion"   % bootstrapPlayVersion,
    "org.typelevel"             %%  "cats-core"                       % catsVersion
  )

  val test = Seq(
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion"     %   hmrcMongoVersion          % Test,
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"      %   bootstrapPlayVersion      % Test,
    "org.scalatest"             %%  "scalatest"                       %   scalaTestVersion          % Test,
    "org.scalatestplus.play"    %%  "scalatestplus-play"              %   scalaTestPlusPlayVersion  % Test,
    "org.playframework"         %%  "play-test"                       %   playTestVersion           % Test,
    "com.vladsch.flexmark"      %   "flexmark-all"                    %   flexmarkVersion           % Test,
    "org.scalatestplus"         %%  "mockito-4-5"                     % s"$scalaTestVersion.0"      % Test,
    "org.wiremock"              %   "wiremock-standalone"             %   wireMockVersion           % Test
  )

  def apply() = compile ++ test
}


