import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "business-registration-dynamic-stub"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;view.*;models.*;config.*;poc.view.*;poc.config.*;.*(AuthService|BuildInfo|Routes).*",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) : _*)
  .settings(scalaSettings: _*)
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scoverageSettings,
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.0",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.0",
      "com.typesafe.akka" %% "akka-stream" % "2.6.0",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.0",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.0"
    ),
    retrieveManaged := true,
    scalaVersion := "2.12.12"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())
  .settings(
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo
  )

javaOptions in Test += "-Dlogger.resource=logback-test.xml"