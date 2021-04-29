
import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

object AppDependencies {

  private val bootstrapPlay26Version = "3.2.0"
  private val scalaTestVersion = "3.2.8"
  private val pegdownVersion = "1.6.0"
  private val simpleReactivemongoVersion = "8.0.0-play-26"
  private val reactivemongoTestVersion = "5.0.0-play-26"

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
        "org.mockito" % "mockito-core" % "3.9.0" % scope,
        "org.scalatestplus" %% "mockito-3-4" % "3.2.8.0" % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
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
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


