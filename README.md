
Swatch is an (hopefully) very simple wrapper around the
[Java 7 WatchService API](http://docs.oracle.com/javase/tutorial/essential/io/notification.html).

Usage is pretty straightforward:

```scala
import com.mirkocaserta.swatch.Swatch._

watch("plugins", Seq(Create, Modify, Delete), println, true)
```

This registers a [WatchService](http://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html)
on the `plugins` directory and its subdirectories (because of
the `true` value being passed to the `recursive` argument).

The `WatchService` will watch for creation, modification and
deletion of files. Notifications will get sent to the println
function.

If you want (and you sure will) customize your notifications
listener, it's as easy as defining a function such as:

```scala
import com.mirkocaserta.swatch.Swatch._

val listener = (event: SwatchEvent) ⇒ {
  println(s"got an event: $event")

  event match {
    case Create(path) ⇒ println(s"a file was created with path '$path'")
    case Modify(path) ⇒ println(s"a file was modified with path '$path'")
    case Delete(path) ⇒ println(s"a file was deleted with path '$path'")
  }
}

watch("plugins", Seq(Create, Modify, Delete), listener, true)
```

An actor wrapper is also provided. Usage looks like this:

```scala
import akka.actor.{Props, ActorSystem}
import com.mirkocaserta.swatch.Swatch._
import com.mirkocaserta.swatch.SwatchActor

val swatch = system.actorOf(Props[SwatchActor])
swatch ! Watch("plugins", Seq(Create, Modify, Delete), true)
```

Your sending actor will then start receiving `SwatchEvent`
messages. You can optionally specify a different actor to
be notified of `SwatchEvent`s, via the optional `listener`
parameter.