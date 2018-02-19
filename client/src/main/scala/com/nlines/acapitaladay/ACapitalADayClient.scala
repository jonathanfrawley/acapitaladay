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
  def endLine = js.Dynamic.literal(countryUrl="endLine",flagSrc="",countryName="",capital="").asInstanceOf[CountryMetadata]
}

trait CountryMetadata extends js.Object {
  val countryUrl: String = js.native
  val flagSrc: String = js.native
  val countryName: String = js.native
  val capital: String = js.native
}

object CountryConstants {
  val NumCountries: Int = 193
}

object ACapitalADayClient {
  val capital: Array[Var[String]] = Array.fill(CountryConstants.NumCountries)(Var[String](""))
  val correctCapital: Var[String] = Var[String]("")
  val capitalGuess: Array[Var[String]] = Array.fill(CountryConstants.NumCountries)(Var[String](""))
  val capitalGuessBoxClass: Var[String] = Var[String]("form-control is-valid")
  val metadata: Var[js.Array[CountryMetadata]] = Var[js.Array[CountryMetadata]](js.Array())
  val index: Var[Int] = Var[Int](-1)

  // TODO: Change this to be more clever
  val startDate = "2017-11-17"
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val oldDate = LocalDate.parse(startDate, formatter)
  val currentDate = LocalDate.now().format(formatter)
  val newDate = LocalDate.parse(currentDate, formatter)

  @dom
  def capitalDiv: Binding[Div] = {
    <div class="row">
      <div class="col text-center">
        <h5>{ capital(index.bind).bind }</h5>
      </div>
    </div>
  }

  @dom
  def capitalGuessBoxInput: Binding[Input] = {
    <input id="capitalGuessBox" type="text" class={capitalGuessBoxClass.bind} data:aria-label="Username" data:aria-describedby="basic-addon1" disabled={capital(index.bind).bind == correctCapital.bind} oninput={ event: Event =>
      capitalGuess(index.value) := capitalGuessBox.value
      if (correctCapital.value.equalsIgnoreCase(capitalGuess(index.value).value)) {
        capital(index.value) := correctCapital.value
      }
      val newClass = if (correctCapital.value.toLowerCase.startsWith(capitalGuessBox.value.toLowerCase)) "form-control is-valid"
      else "form-control is-invalid"
      capitalGuessBoxClass := newClass
    }/>
  }

  @dom
  def capitalGuessDiv: Binding[Div] = {
    if(capital(index.bind).bind != correctCapital.bind) {
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
    if(capital(index.bind).bind != correctCapital.bind) {
      <div class="row">
        <div class="col text-center">
          <button type="button"
                  class="button"
                  onclick={event: Event =>
                    capital(index.value) := correctCapital.value
                    }>I give up</button>
        </div>
      </div>
    } else <div class="row"></div>
  }

  @dom
  def flagImgDiv(idx: Int, countryMetadata: CountryMetadata): Binding[Div] =
    if(countryMetadata.countryUrl == "endLine") {
      <div class="w-100"></div>
    } else if(countryMetadata.countryUrl == "empty") {
      <div class="col col-xs-7 px-0">
      </div>
    } else {
      <div class="col col-xs-7 px-0" >
        <!--<a href={s"/country/${idx}"}>-->
        <a href="#" onclick={event: Event => index := idx }>
          <img src={countryMetadata.flagSrc} class="img-thumbnail" style="max-height: 120px;"></img>
        </a>
      </div>
    }

  @dom
  def flagDiv(): Binding[Div] = {
    val groups: Seq[js.Array[CountryMetadata]] = metadata.value.grouped(7).toSeq
    val results: Seq[CountryMetadata] = groups.foldLeft(Seq[CountryMetadata]())(
      (resultSoFar: Seq[CountryMetadata], array: js.Array[CountryMetadata]) => {
        resultSoFar ++ Seq(CountryMetadata.endLine) ++ array.toSeq.padTo(7, CountryMetadata.empty)
      })

    var idx = -1
    <div class="row mt-3">
      {
        (for (row <- Constants(results: _*)) yield {
          if(!row.countryUrl.startsWith("endLine") && !row.countryUrl.startsWith("empty")) idx += 1
          flagImgDiv(idx, row).bind
        })
      }
    </div>
  }

  @dom
  def mainElement(): Binding[Div] = {
    FutureBinding(Ajax.get("/assets/json/countries.json")).bind match {
      case None =>
        <div><p>Loading...</p></div>
      case Some(Failure(resp)) =>
        <div><p>Failure</p></div>
      case Some(Success(resp)) =>
        metadata := JSON.parse(resp.responseText).asInstanceOf[js.Array[CountryMetadata]]

        val flagSrc = metadata.value(index.bind).flagSrc
        val countryName: String = metadata.value(index.bind).countryName
        val capitalName: String = metadata.value(index.bind).capital
        val countryUrl: String = metadata.value(index.bind).countryUrl
        correctCapital := capitalName

        <div class="container">
          <div class="row h-100">
            <div class="col">
            </div>
            <div class="col-sm-3">
              <div class="row h-100 align-items-center">
                <div class="col">
                  <button type="button" class="btn btn-default"
                    onclick={event: Event =>
                      index := 0
                    }>
                    <span class="oi oi-media-step-backward"></span>
                  </button>
                </div>
                <div class="col">
                  <button type="button" class="btn btn-default"
                    onclick={event: Event =>
                      index := scala.math.max(index.value - 1, 0)
                    }>
                    <span class="oi oi-arrow-left"></span>
                  </button>
                </div>
              </div>
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
            <div class="col-sm-3">
              <div class="row h-100 align-items-center">
                <div class="col">
                  <button type="button" class="btn btn-default"
                    onclick={event: Event =>
                      index := scala.math.min(index.value + 1, (CountryConstants.NumCountries-1))
                    }>
                    <span class="oi oi-arrow-right"></span>
                  </button>
                </div>
                <div class="col">
                  <button type="button" class="btn btn-default"
                    onclick={event: Event =>
                      index := CountryConstants.NumCountries-1
                    }>
                    <span class="oi oi-media-step-forward"></span>
                  </button>
                </div>
              </div>
            </div>
            <div class="col">
            </div>
          </div>

          <!-- Image stuff: Disabled as not finished. -->
          <!--{ flagDiv.bind }-->
        </div>
    }
  }

  def main(args: Array[String]): Unit = {
    //val idx = if (js.Dynamic.global.window.index.asInstanceOf[Int] == -1) (newDate.toEpochDay() - oldDate.toEpochDay()).toInt else index.bind
    val jsIndex = js.Dynamic.global.window.index.asInstanceOf[Int]
    index := (if (jsIndex == -1) (newDate.toEpochDay() - oldDate.toEpochDay()).toInt else jsIndex)
    dom.render(org.scalajs.dom.document.body, mainElement)
  }
}
