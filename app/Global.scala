import java.util.concurrent.TimeUnit
import akka.actor.{ActorRef, ActorSystem, Props}
import manager.SequencerActor
import play.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Global extends GlobalSettings {

  lazy val actorRef:ActorRef = system.actorOf(Props(new SequencerActor()), name = "monitorActor")

  private def startSequencer() = {
    val tickInterval  = new FiniteDuration(1, TimeUnit.SECONDS)
    val cancellable = system.scheduler.schedule(tickInterval, tickInterval, actorRef, "tick") //initialDelay, delay, Actor, Message
  }
  val system: ActorSystem = ActorSystem("K8055-Sequencer")
  override def onStart(app: Application) {
//    Logger.info("Application has started")
    startSequencer()
  }

  override def onStop(app: Application) {
//    Logger.info("Application shutdown...")
    actorRef ! "stop"
  }
}
