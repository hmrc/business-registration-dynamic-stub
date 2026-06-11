import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

val appName = "business-registration-dynamic-stub"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.18"

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
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scoverageSettings,
    scalacOptions += "-Xlint:-unused",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
  )

lazy val it = project.in(file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings(true))
  .settings(
    libraryDependencies ++= AppDependencies(),
    addTestReportOption(Test, "int-test-reports")
  )

Test / javaOptions += "-Dlogger.resource=logback-test.xml"