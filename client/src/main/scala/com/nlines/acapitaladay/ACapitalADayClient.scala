package com.nlines.acapitaladay

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.scalajs.js
import scala.scalajs.js.JSON

@js.native
trait CountryMetadata extends js.Object {
  val countryUrl: String = js.native
  val flagSrc: String = js.native
  val countryName: String = js.native
  val capital: String = js.native
}

object ACapitalADayClient {

  def main(args: Array[String]): Unit = {
    //dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    //dom.document.getElementById("scalajsShoutOut").textContent = "blah"

    Ajax.get("/assets/json/countries.json").map { r =>
      val testJson = """[{"countryUrl":"https://en.wikipedia.org/wiki/Albania","flagSrc":"//upload.wikimedia.org/wikipedia/commons/thumb/3/36/Flag_of_Albania.svg/125px-Flag_of_Albania.svg.png","countryName":"Albania","capital":"Tirana"}]"""
      ///val metadata: Seq[CountryMetadata] = (JSON.parse(r.responseText).asInstanceOf[js.Array[js.Dynamic]]).map (_.asInstanceOf[CountryMetadata] )
      //val metadata = JSON.parse(testJson).asInstanceOf[CountryMetadata]
      //val metadata = JSON.parse(testJson).asInstanceOf[js.Array[CountryMetadata]]
      println(s"responseText: ${r.responseText}")
      val metadata = JSON.parse(r.responseText).asInstanceOf[js.Array[CountryMetadata]]
      ///dom.document.getElementById("scalajsShoutOut").textContent = metadata.head.countryUrl
      println(metadata.head.countryUrl)
      //dom.document.getElementById("scalajsShoutOut").textContent = "blah"

      //val metadata: Seq[CountryMetadata] = (JSON.parse(r.responseText).asInstanceOf[js.Array[js.Dynamic]]).map (_.asInstanceOf[CountryMetadata] )
    }
  }
}
