import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "business-registration-dynamic-stub"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;view.*;models.*;config.*;poc.view.*;poc.config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin): _*)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scoverageSettings,
    scalacOptions += "-Xlint:-unused",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    scalaVersion := "2.13.10"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

Test / javaOptions += "-Dlogger.resource=logback-test.xml"