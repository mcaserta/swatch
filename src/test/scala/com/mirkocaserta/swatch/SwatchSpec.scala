package com.mirkocaserta.swatch

import java.nio.file.{Path, Paths, Files}
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification

class SwatchSpec extends Specification {

  import collection.mutable.Queue
  import Swatch._

  val log = LoggerFactory.getLogger(getClass)

  "Swatch" should {
    "notify the listener when a file is created" in {
      val dir = tmp
      val events: Queue[SwatchEvent] = Queue()

      val listener = (ev: SwatchEvent) ⇒ {
        events enqueue ev
      }
      watch(dir, Seq(Create), listener)
      var tmpFile: Option[Path] = None
      doFileSystemWork {
        tmpFile = Some(Files.createTempFile(dir, "file-", ""))
        log.debug(s"tmpFile=$tmpFile")
      }
      tmpFile must beSome[Path]
      events.size must be equalTo 1
      events.dequeue must be equalTo Create(tmpFile.get)
    }
  }

  private[this] def doFileSystemWork(block: ⇒ Unit) {
    sleep(3)
    block
    // the underlying OS notifications' delivery takes quite a while
    sleep(15)
  }

  private[this] def tmp = Files.createTempDirectory(Paths.get("target"), "watch-")

  private[this] def sleep(seconds: Int) {
    log.debug(s"sleeping for $seconds seconds...")
    Thread.sleep(seconds * 1000)
  }

}
