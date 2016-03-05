package manager

import connectors.K8055
import model.{DeviceState, EventType, Step}
import play.api.Logger


object SequenceExecutionManager extends SequenceExecutionManager{
  override val sequenceManager = SequenceManager
}

trait SequenceExecutionManager {
  val sequenceManager: SequenceManager
  val START_STEP = 1
  var currentStep = START_STEP
  var running = false

  def runSequence() = {
    println("#### runSequence")
    val oStep: Option[Step] = sequenceManager.getStep(currentStep)
    oStep.fold({
      currentStep = START_STEP
      running = false
    })(step => performStep(step))
  }

  def performStep(step: Step): Unit = {
    println("#### performStep")
    step.eventType match {
      case (EventType.ON) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); currentStep += 1 //Digital Out
      case (EventType.OFF) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); currentStep += 1 //Digital/Analogue Out/Monitor
      case (EventType.SET_VALUE) => currentStep += 1
      case (EventType.WAIT_RISING) => currentStep += 1
      case (EventType.WAIT_FALLING) => currentStep += 1
      case (EventType.WAIT_TIME) => currentStep += 1 //Sequencer.runWaitTime(step, component) //Any
      case (EventType.WAIT_ON) => currentStep += 1
      case (EventType.WAIT_OFF) => currentStep += 1
      case (EventType.WAIT_COUNT) => currentStep += 1

      case _ => Logger.warn("Bad Step Type: " + step)
    }
  }

}
