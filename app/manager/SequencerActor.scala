package manager

import akka.actor.Actor
import model.{SequenceState, Step, Sequence}
import play.api.Logger

class SequencerActor extends SequencerActorTrait with Actor{
  override val sequencerManager = SequenceManager

  def receive = {
    case "tick" => processSteps()
    case "stop" => context.stop(self)
    case _ => Logger.error("unknown message in MonitorActor")
  }
}

trait SequencerActorTrait{
  val sequencerManager:SequenceManager

  def processSteps() = {}
}
