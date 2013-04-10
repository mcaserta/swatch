package com.mirkocaserta.swatch

import Swatch._

object Example extends App {
  val twoMinutes = 2 * 60 * 1000

  val listener = (ev: SwatchEvent) ⇒ {
    println(ev)
  }

  watch("src/test/", true, listener, Create, Modify, Delete)

  Thread.sleep(twoMinutes)
}
