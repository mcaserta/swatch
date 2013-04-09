package com.mirkocaserta.swatch

import java.nio.file.{WatchEvent, Paths, Path, FileSystems}
import concurrent.future
import scala.util.{Failure, Success, Try}
import java.nio.file.WatchEvent.Kind

object Swatch {
  import concurrent.ExecutionContext.Implicits.global

  private[this] val watchService = FileSystems.getDefault.newWatchService

  sealed trait EventType

  case object Create extends EventType

  case object Modify extends EventType

  case object Delete extends EventType

  case object Overflow extends EventType

  case class SwatchEvent(tpe: EventType, path: Option[Path] = None)

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

  def watch(path: String,
            listener: (SwatchEvent) ⇒ Unit,
            eventTypes: EventType*) {
    watch(Paths.get(path), listener, eventTypes: _*)
  }

  def watch(path: Path,
            listener: (SwatchEvent) ⇒ Unit,
            eventTypes: EventType*) {
    path.register(watchService, eventTypes map eventType2Kind: _*)

    var loop = true

    future {
      import collection.JavaConversions._

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
                    listener(SwatchEvent(tpe, Some(ev.context)))
                    if (!key.reset) loop = false
                }
            }
          case Failure(e) ⇒ // ignored for now. TODO: do something?
        }
      }
    }
  }

}

