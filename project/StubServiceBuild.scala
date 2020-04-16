import sbt._


object StubServiceBuild extends Build with MicroService {

  import scala.util.Properties.envOrElse

  val appName = "business-registration-dynamic-stub"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._


  private val bootstrapPlay26Version = "1.7.0"
  private val scalaTestVersion = "3.0.8"
  private val pegdownVersion = "1.6.0"
  private val simpleReactivemongoVersion = "7.20.0-play-26"
  private val reactivemongoTestVersion = "4.19.0-play-26"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapPlay26Version,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "org.typelevel" %% "cats" % "0.9.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


