package com.nlines.acapitaladay

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.thoughtworks.binding.Binding.{BindingSeq, Constants, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.FutureBinding
import org.scalajs.dom.{Event, Node}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.{Div, Input, Table}

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}


object CountryMetadata {
  def empty = js.Dynamic.literal(countryUrl="empty",flagSrc="",countryName="",capital="").asInstanceOf[CountryMetadata]
}

sealed trait CountryMetadataType {}
case class EmptyCountryMetadataType() extends CountryMetadataType
case class ValidCountryMetadataType() extends CountryMetadataType

trait CountryMetadata extends js.Object {
  val countryUrl: String = js.native
  val flagSrc: String = js.native
  val countryName: String = js.native
  val capital: String = js.native
  //val `type`: CountryMetadataType = ValidCountryMetadataType()
}

/*
@js.native
class EmptyCountryMetadata extends CountryMetadata {
  override val countryUrl: String = ""
  override val flagSrc: String = ""
  override val countryName: String = ""
  override val capital: String = ""
  override val `type`: CountryMetadataType = EmptyCountryMetadataType()
}
*/

object ACapitalADayClient {

  val capital: Var[String] = Var[String]("")
  val correctCapital: Var[String] = Var[String]("")
  val capitalGuess: Var[String] = Var[String]("")
  val capitalGuessBoxClass: Var[String] = Var[String]("form-control is-valid")
  val won: Var[Boolean] = Var[Boolean](false)
  val metadata: Var[js.Array[CountryMetadata]] = Var[js.Array[CountryMetadata]](js.Array())


  // TODO: Change this to be more clever
  val startDate = "2017-11-18"
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val oldDate = LocalDate.parse(startDate, formatter)
  //val currentDate = "2018-02-07"
  val currentDate = LocalDate.now().format(formatter)
  val newDate = LocalDate.parse(currentDate, formatter)

  @dom
  def capitalDiv: Binding[Div] = {
    <div class="row">
      <div class="col text-center">
        <h5>{ capital.bind }</h5>
      </div>
    </div>
  }

  @dom
  def capitalGuessBoxInput: Binding[Input] = {
    <input id="capitalGuessBox" type="text" class={capitalGuessBoxClass.bind} data:aria-label="Username" data:aria-describedby="basic-addon1" disabled={won.bind} oninput={ event: Event =>
      capitalGuess := capitalGuessBox.value
      if (correctCapital.value.equalsIgnoreCase(capitalGuess.value)) {
        won := true
        capital := correctCapital.value
      }
      val newClass = if (correctCapital.value.toLowerCase.startsWith(capitalGuessBox.value.toLowerCase)) "form-control is-valid"
      else "form-control is-invalid"
      capitalGuessBoxClass := newClass
    }/>
  }

  @dom
  def capitalGuessDiv: Binding[Div] = {
    //val capitalStr = capital.value
    //val capitalGuessStr = capitalGuess.value
    //val classes = if(capitalStr.startsWith(capitalGuessStr)) "form-control is-valid" else "form-control is-invalid"
    if(! won.bind) {
      <div class="row">
        <div class="col text-center">
          <div class="input-group input-group-sm mb-3">
            <div class="input-group-prepend is-invalid">
              <span class="input-group-text">Capital</span>{capitalGuessBoxInput.bind}
            </div>
          </div>
        </div>
      </div>
    } else {
      <div></div>
    }
  }

  @dom
  def giveUpButton(capitalName: String): Binding[Div] = {
    if(! won.bind) {
      <div class="row">
        <div class="col text-center">
          <button type="button"
                  class="button"
                  onclick={event: Event =>
                    won := true
                    capital := capitalName }>I give up</button>
        </div>
      </div>
    } else <div class="row"></div>
  }

  /*
  def flagImgs(metadata: js.Array[CountryMetadata]): Seq[Div] = {
    for (row <- metadata) yield {
      <div class="col col-xs-7 px-0">
        <img src={row.flagSrc} class="img-thumbnail"></img>
      </div>
    }
  }
  */

  @dom
  def flagImgDiv(countryMetadata: CountryMetadata): Binding[Div] =
    if(countryMetadata.countryUrl == "empty") {
      <div class="w-100"></div>
    } else {
      <div class="col col-xs-7 px-0">
        <img src={countryMetadata.flagSrc} class="img-thumbnail"></img>
      </div>
    }

  @dom
  def flagDiv(): Binding[Div] = {
    /*
    <div class="row mt-3">
      {
        var i = 0
        (for (row <- Constants(metadata.bind: _*)) yield {
          i += 1
          flagImgDiv(row.flagSrc, i).bind
        })
      }
    </div>
    */
    //val groups: BindingSeq[CountryMetadata] = Constants(metadata.bind: _*)
    val groups: Seq[js.Array[CountryMetadata]] = metadata.value.grouped(7).toSeq
    val results: Seq[CountryMetadata] = groups.foldLeft(Seq[CountryMetadata]())(
      (resultSoFar: Seq[CountryMetadata], array: js.Array[CountryMetadata]) =>
        resultSoFar ++ Seq(CountryMetadata.empty) ++ array)

    <div class="row align-items-center mt-3">
      {
      /*
        (for (group <- groups) yield {
          for (row <- group) yield flagImgDiv(row.flagSrc).bind
        }).flatten
        */
        (for (row <- Constants(results: _*)) yield {
          flagImgDiv(row).bind
        })
      }
    </div>
    /*
    <div class="row mt-3">
      <div class="col col-xs-7 px-0">
        <img src="https://upload.wikimedia.org/wikipedia/commons/3/36/Flag_of_Albania.svg" class="img-thumbnail"></img>
      </div>
    </div>
    */
  }

  @dom
  def table: Binding[Div] = {
    FutureBinding(Ajax.get("/assets/json/countries.json")).bind match {
      case None =>
        <div><p>Loading...</p></div>
      case Some(Failure(resp)) =>
        <div><p>Failure</p></div>
      case Some(Success(resp)) =>
        metadata := JSON.parse(resp.responseText).asInstanceOf[js.Array[CountryMetadata]]
        ///dom.document.getElementById("scalajsShoutOut").textContent = metadata.head.countryUrl
        //println(metadata.head.countryUrl)
        //println(metadata.head.countryUrl)
        //dom.document.getElementById("scalajsShoutOut").textContent = "blah"
        val idx = (newDate.toEpochDay() - oldDate.toEpochDay()).toInt
        val flagSrc = metadata.value(idx).flagSrc
        val countryName: String = metadata.value(idx).countryName
        val capitalName: String = metadata.value(idx).capital
        val countryUrl: String = metadata.value(idx).countryUrl
        correctCapital := capitalName

        <div class="container">
          <div class="row">
            <div class="col">
            </div>
            <div class="col-sm-5">
              <div class="row">
                <div class="col text-center">
                  <img src={flagSrc} class="img-fluid rounded border" width={700} />
                </div>
              </div>
              <div class="row">
                <div class="col text-center">
                  <a href={countryUrl}><h4>{countryName}</h4></a>
                </div>
              </div>
              { capitalDiv.bind }
              { capitalGuessDiv.bind }
              { giveUpButton(capitalName).bind }
            </div>
            <div class="col">
            </div>
          </div>

          <!-- Image stuff-->
          { flagDiv.bind }
        </div>
    }
  }



  def main(args: Array[String]): Unit = {
    //dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    //dom.document.getElementById("scalajsShoutOut").textContent = "blah"

    /*
    val startDate = "2017-11-18"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val oldDate = LocalDate.parse(startDate, formatter)
    //val currentDate = "2018-02-07"
    val currentDate = LocalDate.now().format(formatter)
    val newDate = LocalDate.parse(currentDate, formatter)
    println(newDate.toEpochDay() - oldDate.toEpochDay())
    */

    /*
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
      dom.document.getElementById("countryLink").setAttribute("href", metadata(idx).countryUrl)

      jQuery("#capitalButton").on("click", () =>
        dom.document.getElementById("capital").textContent = s"Capital: ${metadata(idx).capital}"
      )
      //val metadata: Seq[CountryMetadata] = (JSON.parse(r.responseText).asInstanceOf[js.Array[js.Dynamic]]).map (_.asInstanceOf[CountryMetadata] )
    }
    */
    dom.render(org.scalajs.dom.document.body, table)
  }
}
