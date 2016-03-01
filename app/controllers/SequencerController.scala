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

  def start() = Action.async {
    implicit request => {
      Future.successful(Ok("Ok"))
    }
  }

  def stop() = Action.async {
    implicit request => {
      Future.successful(Ok("Ok"))
    }
  }

  def reset() = Action.async {
    implicit request => {
      Future.successful(Ok("Ok"))
    }
  }

  def next() = Action.async {
    implicit request => {
      Future.successful(Ok("Ok"))
    }
  }

  def previous() = Action.async {
    implicit request => {
      Future.successful(Ok("Ok"))
    }
  }

}