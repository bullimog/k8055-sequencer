package manager

import akka.actor.Actor
import model.{SequenceState, Step, Sequence}

import play.api.Logger

class SequencerActor extends SequencerActorTrait with Actor{
  override val sequenceExecutionManager = SequenceExecutionManager

  def receive = {
    case "tick" => sequenceExecutionManager.runSequence()
    case "stop" => context.stop(self)
    case _ => Logger.error("unknown message in MonitorActor")
  }
}

trait SequencerActorTrait{
  val sequenceExecutionManager:SequenceExecutionManager


}
