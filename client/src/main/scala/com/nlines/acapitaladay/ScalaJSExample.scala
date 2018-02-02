package com.nlines.acapitaladay

import com.nlines.acapitaladay.shared.SharedMessages
import org.scalajs.dom

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
  }
}
