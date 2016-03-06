package manager

import connectors.K8055
import model._
import play.api.Logger


object SequenceExecutionManager extends SequenceExecutionManager{
  override val sequenceManager = SequenceManager
  override val timer = Timer
}

trait SequenceExecutionManager {
  val sequenceManager: SequenceManager
  val timer: Timer
  val START_STEP = 0
  var currentStep = START_STEP
  var running = false

  def runSequence():Unit = {
//    println("#### runSequence")
    if(running)
      sequenceManager.getStep(currentStep).fold()(step => performStep(step))
  }

  def performStep(step: Step): Unit = {
    println("#### performStep "+step)
    step.eventType match {
      case (EventType.ON) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); currentStep += 1 //Digital Out
      case (EventType.OFF) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); currentStep += 1 //Digital/Analogue Out/Monitor
      case (EventType.SET_VALUE) => currentStep += 1
      case (EventType.WAIT_RISING) => currentStep += 1
      case (EventType.WAIT_FALLING) => currentStep += 1
      case (EventType.WAIT_TIME) => runWaitTime(step) //Any
      case (EventType.WAIT_ON) => currentStep += 1
      case (EventType.WAIT_OFF) => currentStep += 1
      case (EventType.WAIT_COUNT) => currentStep += 1
      case _ => Logger.warn("Bad Step Type: " + step)
    }
  }


  def runWaitTime(step:Step): Unit = {
    if(timer.waitingFor(step.id)) { //already running
      if(timer.finished(step.id)){currentStep += 1; timer.step = -1}
    }
    else{ //set up a Timer
      step.value match {
        case Some (duration) => { timer.setTimer( step.id, step.value.getOrElse(0))}
        case _ => Logger.warn("No duration specified,  can't wait for: " + step)
      }
    }
  }

}
