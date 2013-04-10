package com.mirkocaserta.swatch

import org.specs2.mutable.Specification
import java.nio.file.{Path, Paths, Files}

class SwatchSpec extends Specification {

  import collection.mutable.Queue
  import Swatch._

  "Swatch" should {
    "notify the listener when a file is created" in {
      val dir = tmp
      val events: Queue[SwatchEvent] = Queue()

      val listener = (ev: SwatchEvent) ⇒ {
        events enqueue ev
      }
      watch(dir, true, listener, Create)
      var tmpFile: Option[Path] = None
      doFileSystemWork {
         tmpFile = Some(Files.createTempFile(dir, "file-", ""))
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
    println(s"sleeping for $seconds seconds...")
    Thread.sleep(seconds * 1000)
  }

}
