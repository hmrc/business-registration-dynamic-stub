import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._


trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

  import play.sbt.routes.RoutesKeys.routesGenerator


  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

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
    .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(majorVersion := 0)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      scoverageSettings,
      libraryDependencies ++= appDependencies,
      dependencyOverrides ++= Set(
        "com.typesafe.akka" %% "akka-actor" % "2.5.23",
        "com.typesafe.akka" %% "akka-protobuf" % "2.5.23",
        "com.typesafe.akka" %% "akka-slf4j" % "2.5.23",
        "com.typesafe.akka" %% "akka-stream" % "2.5.23"
      ),
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      scalaVersion := "2.11.12"
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(integrationTestSettings())
    .settings(
      resolvers += Resolver.bintrayRepo("hmrc", "releases"),
      resolvers += Resolver.jcenterRepo
    )

}
