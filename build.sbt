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
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    scalaVersion := "2.12.15",
    scalacOptions += "-Ypartial-unification"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())

javaOptions in Test += "-Dlogger.resource=logback-test.xml"