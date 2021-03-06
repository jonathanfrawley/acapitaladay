package com.nlines.acapitaladay.controllers

import javax.inject._

import com.nlines.acapitaladay.shared.SharedMessages
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index(idx: Int = -1) = Action {
    Ok(views.html.index(idx))
  }

}
