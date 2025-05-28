
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion                   =  "-play-30"
  private val bootstrapPlayVersion          =  "8.6.0"
  private val scalaTestVersion              =  "3.2.19"
  private val scalaTestPlusPlayVersion      =  "7.0.1"
  private val flexmarkVersion               =  "0.64.8"
  private val wireMockVersion               =  "3.13.0"
  private val hmrcMongoVersion              =  "2.6.0"
  private val catsVersion                   =  "2.13.0"
  private val playTestVersion               =  "3.0.7"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo$playVersion"          % hmrcMongoVersion,
    "uk.gov.hmrc"               %% s"bootstrap-backend$playVersion"   % bootstrapPlayVersion,
    "org.typelevel"             %%  "cats-core"                       % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion"     %   hmrcMongoVersion          % Test,
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"      %   bootstrapPlayVersion      % Test,
    "org.scalatest"             %%  "scalatest"                       %   scalaTestVersion          % Test,
    "org.scalatestplus.play"    %%  "scalatestplus-play"              %   scalaTestPlusPlayVersion  % Test,
    "org.playframework"         %%  "play-test"                       %   playTestVersion           % Test,
    "com.vladsch.flexmark"      %   "flexmark-all"                    %   flexmarkVersion           % Test,
    "org.scalatestplus"         %%  "mockito-4-5"                     % "3.2.12.0"      % Test,
    "org.wiremock"              %   "wiremock-standalone"             %   wireMockVersion           % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
