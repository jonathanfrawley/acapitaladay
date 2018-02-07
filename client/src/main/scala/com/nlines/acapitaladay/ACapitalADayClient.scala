package com.nlines.acapitaladay

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val startDate = "2017-11-18"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val oldDate = LocalDate.parse(startDate, formatter)
    //val currentDate = "2018-02-07"
    val currentDate = LocalDate.now().format(formatter)
    val newDate = LocalDate.parse(currentDate, formatter)
    println(newDate.toEpochDay() - oldDate.toEpochDay())

    Ajax.get("/assets/json/countries.json").map { r =>
      val testJson = """[{"countryUrl":"https://en.wikipedia.org/wiki/Albania","flagSrc":"//upload.wikimedia.org/wikipedia/commons/thumb/3/36/Flag_of_Albania.svg/125px-Flag_of_Albania.svg.png","countryName":"Albania","capital":"Tirana"}]"""
      ///val metadata: Seq[CountryMetadata] = (JSON.parse(r.responseText).asInstanceOf[js.Array[js.Dynamic]]).map (_.asInstanceOf[CountryMetadata] )
      //val metadata = JSON.parse(testJson).asInstanceOf[CountryMetadata]
      //val metadata = JSON.parse(testJson).asInstanceOf[js.Array[CountryMetadata]]
      //println(s"responseText: ${r.responseText}")
      val metadata = JSON.parse(r.responseText).asInstanceOf[js.Array[CountryMetadata]]
      ///dom.document.getElementById("scalajsShoutOut").textContent = metadata.head.countryUrl
      //println(metadata.head.countryUrl)
      //println(metadata.head.countryUrl)
      //dom.document.getElementById("scalajsShoutOut").textContent = "blah"
      val idx = (newDate.toEpochDay() - oldDate.toEpochDay()).toInt
      dom.document.getElementById("flagImg").setAttribute("src", metadata(idx).flagSrc)
      dom.document.getElementById("countryName").textContent = metadata(idx).countryName
      dom.document.getElementById("countryName").setAttribute("href", metadata(idx).countryUrl)
      dom.document.getElementById("capital").textContent = metadata(idx).capital

      //val metadata: Seq[CountryMetadata] = (JSON.parse(r.responseText).asInstanceOf[js.Array[js.Dynamic]]).map (_.asInstanceOf[CountryMetadata] )
    }
  }
}
