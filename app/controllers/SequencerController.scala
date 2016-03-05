package controllers

import manager.SequenceManager
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SequencerController extends Controller {

  def sequence() = Action.async {

    implicit request => {
      SequenceManager.getReadableSequence.map(rs =>
        Ok(Json.toJson(rs).toString())
      )
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