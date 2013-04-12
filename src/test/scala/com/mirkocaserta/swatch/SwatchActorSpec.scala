package com.mirkocaserta.swatch

import akka.actor.{Props, ActorSystem}
import akka.testkit.{DefaultTimeout, TestKit, ImplicitSender}
import com.typesafe.config.ConfigFactory
import concurrent.duration._
import java.nio.file.{Paths, Files}
import language.postfixOps
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

class SwatchActorSpec extends TestKit(ActorSystem("SwatchActorSpec",
  ConfigFactory.parseString(SwatchActorSpec.config)))
with DefaultTimeout with ImplicitSender
with WordSpec with ShouldMatchers with BeforeAndAfterAll {

  import Swatch._

  "A Swatch actor" should {
    "send notifications" in {
      within(15 seconds) {
        val swatch = system.actorOf(Props[SwatchActor])
        val dir = tmp
        swatch ! Watch(dir, Seq(Create))
        Thread.sleep(3000)
        val file = Files.createTempFile(dir, "file-", "")
        expectMsg(Create(file))
      }
    }
  }

  override def afterAll {
    system.shutdown
  }

  private[this] def tmp = Files.createTempDirectory(Paths.get("target"), "watch-actor-")

}

object SwatchActorSpec {
  // test specific configuration
  val config = """
    akka {
      loglevel = "DEBUG"
    }
               """
}