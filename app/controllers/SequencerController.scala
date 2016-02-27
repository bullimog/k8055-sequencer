package controllers

import manager.SequenceManager
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future

class SequencerController extends Controller {

  def sequence() = Action.async {
    implicit request => {
      val json = Json.toJson(SequenceManager.getSequence)
      Future.successful(Ok(json))
    }
  }
}