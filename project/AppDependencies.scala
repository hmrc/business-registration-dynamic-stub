
import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion = "5.16.0"
  private val scalaTestVersion = "3.1.4"
  private val simpleReactivemongoVersion = "8.0.0-play-28"
  private val reactivemongoTestVersion = "5.0.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
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
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
        "org.mockito" % "mockito-core" % "4.0.0" % scope,
        "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
        "com.github.tomakehurst" % "wiremock-jre8-standalone" % "2.31.0" % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}


