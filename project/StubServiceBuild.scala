import sbt._


object StubServiceBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "business-registration-dynamic-stub"
  val appVersion = envOrElse("BUSINESS_REGISTRATION_DYNAMIC_STUB_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._


  private val bookstrapPlay25Version    = "1.5.0"
  private val hmrcTestVersion           = "3.0.0"
  private val scalaTestVersion          = "2.2.6"
  private val pegdownVersion            = "1.6.0"
  private val playReactiveMongoVersion  = "5.2.0"
  
  val compile = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-play-25"  % bookstrapPlay25Version,
    "uk.gov.hmrc"   %% "play-reactivemongo" % playReactiveMongoVersion,
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
        "uk.gov.hmrc"             %% "reactivemongo-test" % "2.0.0"             % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.0"             % scope,
        "org.mockito"             % "mockito-all"         % "1.9.5"             % scope
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
        "uk.gov.hmrc"             %% "reactivemongo-test" % "2.0.0"             % scope,
        "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.0"             % scope,
        "com.github.tomakehurst"  % "wiremock"            % "2.6.0"             % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


