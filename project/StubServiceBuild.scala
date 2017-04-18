import sbt._


object StubServiceBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "business-registration-dynamic-stub"
  val appVersion = envOrElse("BUSINESS_REGISTRATION_DYNAMIC_STUB_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion


  private val microserviceBootstrapVersion = "5.15.0"
  private val playHealthVersion = "2.1.0"
  private val playConfigVersion = "4.3.0"
  private val hmrcTestVersion = "2.3.0"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"
  private val playJsonLoggerVersion = "3.0.0"
  private val playReactivemongoVersion = "5.2.0"
  private val reactiveMongoTest = "2.0.0"
  private val scalaTestPlusVersion = "1.5.1"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "2.6.2"
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % reactiveMongoTest % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


