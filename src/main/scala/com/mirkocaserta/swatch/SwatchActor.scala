package com.mirkocaserta.swatch

import akka.actor.{Actor, ActorLogging}

/**
 * An [[akka.actor.Actor]] wrapper for
 * [[com.mirkocaserta.swatch.Swatch#watch]].
 *
 * The actor is activated by sending to it a
 * [[com.mirkocaserta.swatch.Swatch.Watch]]
 * request. Notifications of filesystem changes
 * will then be sent to the [[akka.actor.ActorRef]]
 * specified in the request message or, if not
 * present, they will get sent to the
 * [[akka.actor.Actor#sender]] ref.
 */
class SwatchActor extends Actor with ActorLogging {

  import Swatch._

  def receive = {
    case Watch(path, eventTypes, recurse, listener) ⇒
      log.debug(s"receive(): got a watch request; path='$path', eventTypes=$eventTypes, recurse=$recurse, listener='$listener'")

      val senderRef = listener getOrElse sender

      val lstnr = {
        event: SwatchEvent ⇒
          log.debug(s"receive(): notifying; event='$event', senderRef='$senderRef'")
          senderRef ! event
      }

      watch(path, eventTypes, lstnr, recurse)
  }

}
