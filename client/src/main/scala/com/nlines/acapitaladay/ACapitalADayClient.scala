package com.nlines.acapitaladay

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.thoughtworks.binding.Binding.{BindingSeq, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.FutureBinding
import org.scalajs.dom.Event
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.{Div, Input, Table}

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

@js.native
trait CountryMetadata extends js.Object {
  val countryUrl: String = js.native
  val flagSrc: String = js.native
  val countryName: String = js.native
  val capital: String = js.native
}

object ACapitalADayClient {

  val capital: Var[String] = Var[String]("")
  val correctCapital: Var[String] = Var[String]("")
  val capitalGuess: Var[String] = Var[String]("")
  val capitalGuessBoxClass: Var[String] = Var[String]("form-control is-valid")
  val won: Var[Boolean] = Var[Boolean](false)


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

  @dom
  def table: Binding[Div] = {
    FutureBinding(Ajax.get("/assets/json/countries.json")).bind match {
      case None =>
        <div><p>Loading...</p></div>
      case Some(Failure(resp)) =>
        <div><p>Failure</p></div>
      case Some(Success(resp)) =>
        val metadata = JSON.parse(resp.responseText).asInstanceOf[js.Array[CountryMetadata]]
        ///dom.document.getElementById("scalajsShoutOut").textContent = metadata.head.countryUrl
        //println(metadata.head.countryUrl)
        //println(metadata.head.countryUrl)
        //dom.document.getElementById("scalajsShoutOut").textContent = "blah"
        val idx = (newDate.toEpochDay() - oldDate.toEpochDay()).toInt
        val flagSrc = metadata(idx).flagSrc
        val countryName: String = metadata(idx).countryName
        val capitalName: String = metadata(idx).capital
        val countryUrl: String = metadata(idx).countryUrl
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
