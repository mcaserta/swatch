package com.mirkocaserta.swatch

import Swatch._

object Example extends App {
  val twoMinutes = 2 * 60 * 1000

  watch("src/test/", Seq(Create, Modify, Delete), println, true)

  Thread.sleep(twoMinutes)
}
