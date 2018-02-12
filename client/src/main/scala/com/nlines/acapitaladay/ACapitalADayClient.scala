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
  val metadata: Var[js.Array[CountryMetadata]] = Var[js.Array[CountryMetadata]](js.Array())

  // TODO: Change this to be more clever
  val startDate = "2017-11-18"
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val oldDate = LocalDate.parse(startDate, formatter)
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
    if(! won.bind) {
      <div class="row">
        <div class="col text-center">
          <div class="input-group input-group-sm mb-3">
            <div class="input-group-prepend is-invalid">
              <span class="input-group-text">Capital</span>
            </div>
            {capitalGuessBoxInput.bind}
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
    val groups: Seq[js.Array[CountryMetadata]] = metadata.value.grouped(7).toSeq
    val results: Seq[CountryMetadata] = groups.foldLeft(Seq[CountryMetadata]())(
      (resultSoFar: Seq[CountryMetadata], array: js.Array[CountryMetadata]) =>
        resultSoFar ++ Seq(CountryMetadata.empty) ++ array)

    <div class="row align-items-center mt-3">
      {
        (for (row <- Constants(results: _*)) yield {
          flagImgDiv(row).bind
        })
      }
    </div>
  }

  @dom
  def mainElement: Binding[Div] = {
    FutureBinding(Ajax.get("/assets/json/countries.json")).bind match {
      case None =>
        <div><p>Loading...</p></div>
      case Some(Failure(resp)) =>
        <div><p>Failure</p></div>
      case Some(Success(resp)) =>
        metadata := JSON.parse(resp.responseText).asInstanceOf[js.Array[CountryMetadata]]
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

          <!-- Image stuff: Disabled as not finished. -->
          <!-- { flagDiv.bind } -->
        </div>
    }
  }

  def main(args: Array[String]): Unit = {
    dom.render(org.scalajs.dom.document.body, mainElement)
  }
}
