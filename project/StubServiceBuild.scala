import sbt._


object StubServiceBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "business-registration-dynamic-stub"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

  private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._


  private val bookstrapPlay25Version    = "4.9.0"
  private val hmrcTestVersion           = "3.6.0-play-25"
  private val scalaTestVersion          = "3.0.0"
  private val pegdownVersion            = "1.6.0"
  private val simpleReactivemongoVersion  = "7.15.0-play-25"
  
  val compile = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-play-25"  % bookstrapPlay25Version,
    "uk.gov.hmrc" %% "simple-reactivemongo"   % simpleReactivemongoVersion,
    "org.typelevel" %% "cats"               % "0.9.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "hmrctest"           % hmrcTestVersion     % scope,
        "org.scalatest"           %% "scalatest"          % scalaTestVersion    % scope,
        "org.pegdown"             % "pegdown"             % pegdownVersion      % scope,
        "com.typesafe.play"       %% "play-test"          % PlayVersion.current % scope,
        "uk.gov.hmrc"             %% "reactivemongo-test" % "4.9.0-play-25"     % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.0"             % scope,
        "org.mockito"             % "mockito-all"         % "2.0.2-beta"        % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc"             %% "hmrctest"           % hmrcTestVersion     % scope,
        "org.scalatest"           %% "scalatest"          % "3.0.0"             % scope,
        "org.pegdown"             % "pegdown"             % "1.5.0"             % scope,
        "com.typesafe.play"       %% "play-test"          % PlayVersion.current % scope,
        "uk.gov.hmrc"             %% "reactivemongo-test" % "4.9.0-play-25"     % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.0"             % scope,
        "com.github.tomakehurst"  % "wiremock"            % "2.6.0"             % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


