package controllers

import manager.{DeviceManager, SequenceManager}
import model.{SequenceState, Step}
import model.Step._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import DeviceManager._

class SequencerController extends Controller {

  def sequence() = Action.async {
    implicit request => {
      val json = Json.toJson(SequenceManager.readAndPopulateSteps(SequenceManager.getSequence))
      Future.successful(Ok(json))
    }
  }
}