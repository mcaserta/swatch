package com.mirkocaserta.swatch

import concurrent.future
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.WatchEvent.Kind
import util.Try
import scala.util.Success
import scala.util.Failure

object Swatch {

  type Listener = (SwatchEvent) ⇒ Unit

  sealed trait EventType

  case object Create extends EventType

  case object Modify extends EventType

  case object Delete extends EventType

  case object Overflow extends EventType

  sealed trait SwatchEvent {
    def path: Path
  }

  case class Create(path: Path) extends SwatchEvent

  case class Modify(path: Path) extends SwatchEvent

  case class Delete(path: Path) extends SwatchEvent

  private[this] implicit def eventType2Kind(et: EventType) = {
    import java.nio.file.StandardWatchEventKinds._

    et match {
      case Create ⇒ ENTRY_CREATE
      case Modify ⇒ ENTRY_MODIFY
      case Delete ⇒ ENTRY_DELETE
      case Overflow ⇒ OVERFLOW
    }
  }

  private[this] implicit def kind2EventType(kind: Kind[Path]) = {
    import java.nio.file.StandardWatchEventKinds._

    kind match {
      case ENTRY_CREATE ⇒ Create
      case ENTRY_MODIFY ⇒ Modify
      case ENTRY_DELETE ⇒ Delete
      case _ ⇒ Overflow
    }
  }

  /**
   * Watch the given path.
   *
   * @param path the path to watch
   * @param recurse should subdirs be watched too?
   * @param listener events will be sent here
   * @param eventTypes event types to watch for
   */
  def watch(path: String,
            recurse: Boolean,
            listener: Listener,
            eventTypes: EventType*) {
    watch(Paths.get(path), recurse, listener, eventTypes: _*)
  }

  /**
   * Watch the given path.
   *
   * @param path the path to watch
   * @param recurse should subdirs be watched too?
   * @param listener events will be sent here
   * @param eventTypes event types to watch for
   */
  def watch(path: Path,
            recurse: Boolean,
            listener: Listener,
            eventTypes: EventType*) {
    val watchService = FileSystems.getDefault.newWatchService

    if (recurse) {
      Files.walkFileTree(path, new SimpleFileVisitor[Path] {
        override def preVisitDirectory(path: Path, attrs: BasicFileAttributes) = {
          watch(path, false, listener, eventTypes: _*)
          FileVisitResult.CONTINUE
        }
      })
    } else path.register(watchService, eventTypes map eventType2Kind: _*)

    import concurrent.ExecutionContext.Implicits.global

    future {
      import collection.JavaConversions._
      var loop = true

      while (loop) {
        Try(watchService.take) match {
          case Success(key) ⇒
            key.pollEvents map {
              event ⇒
                import java.nio.file.StandardWatchEventKinds.OVERFLOW

                event.kind match {
                  case OVERFLOW ⇒ // must be ignored
                  case _ ⇒
                    val ev = event.asInstanceOf[WatchEvent[Path]]
                    val tpe = kind2EventType(ev.kind)
                    val notification = tpe match {
                      case Create ⇒ Create(path.resolve(ev.context))
                      case Modify ⇒ Modify(path.resolve(ev.context))
                      case Delete ⇒ Delete(path.resolve(ev.context))
                    }
                    listener(notification)
                    if (!key.reset) loop = false
                }
            }
          case Failure(e) ⇒ // ignore failure, just as IRL
        }
      }
    }
  }

}

