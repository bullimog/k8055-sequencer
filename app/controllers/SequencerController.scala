package controllers

import manager.{SequenceExecutionManager, SequenceManager}
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

  def sequencerState() = Action.async {
    implicit request => {
        Future.successful(Ok(Json.toJson(SequenceManager.getSequenceState).toString()))
    }
  }

  def start() = Action.async {
    implicit request => {
      SequenceExecutionManager.running = true
      Future.successful(Ok("Ok"))
    }
  }

  def stop() = Action.async {
    implicit request => {
      SequenceExecutionManager.running = false
      Future.successful(Ok("Ok"))
    }
  }

  def reset() = Action.async {
    implicit request => {
      SequenceExecutionManager.currentStep = 0
      SequenceExecutionManager.running = false
      Future.successful(Ok("Ok"))
    }
  }

  def next() = Action.async {
    implicit request => {
      SequenceExecutionManager.incStep()
      Future.successful(Ok("Ok"))
    }
  }

  def previous() = Action.async {
    implicit request => {
      SequenceExecutionManager.decStep()
      Future.successful(Ok("Ok"))
    }
  }

}