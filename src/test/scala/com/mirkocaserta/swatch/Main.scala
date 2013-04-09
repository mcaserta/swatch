package com.mirkocaserta.swatch

// TODO: this needs to become a proper test/spec2
object Main extends App {
  import Swatch._

  val listener = (ev: SwatchEvent) ⇒ {
    println(ev)
  }

  watch("src/test/", listener, Create, Modify, Delete)

  Thread.sleep(2 * 60 * 1000)
}
