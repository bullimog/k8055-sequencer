package manager

import connectors.K8055
import model._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

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
    if(running) {
      if(currentStep < 1) currentStep = 1
      sequenceManager.getStep(currentStep).fold(stopSequencer() )(step => performStep(step))
    }
  }

  def stopSequencer():Unit = {running = false}
  def startSequencer():Unit = {running = true}
  def incStep():Unit = {currentStep += 1}
  def decStep():Unit = {currentStep -= 1}

  def performStep(step: Step): Unit = {
    step.eventType match {
      case (EventType.ON) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); incStep() //Digital Out
      case (EventType.OFF) => K8055.patchDeviceState(DeviceState(step.deviceId, Some(true), None)); incStep() //Digital/Analogue Out/Monitor
      case (EventType.SET_VALUE) => K8055.patchDeviceState(DeviceState(step.deviceId, None, step.value)); incStep()
      case (EventType.WAIT_RISING) => runWaitRising(step)
      case (EventType.WAIT_FALLING) => runWaitFalling(step)
      case (EventType.WAIT_TIME) => runWaitTime(step)
      case (EventType.WAIT_ON) => runWaitOn(step)
      case (EventType.WAIT_OFF) => runWaitOff(step)
      case (EventType.WAIT_COUNT) => incStep()
      case _ => Logger.warn("Bad Step Type: " + step)
    }
  }


  def runWaitTime(step:Step): Unit = {
    if(timer.waitingFor(step.id)) { //already running
      if(timer.finished(step.id)) {
        incStep()
        timer.step = -1
      }
    }
    else{ //set up a Timer
      step.value.fold(Logger.warn("No duration specified,  can't wait for: " + step)) {
        duration => timer.setTimer( step.id, step.value.getOrElse(0))
      }
    }
  }


  def runWaitRising(step: Step) = runWait(step, (x:Int,y:Int) => x >= y)
  def runWaitFalling(step: Step) = runWait(step, (x:Int,y:Int) => x < y)

  def runWait(step: Step, compareFn:(Int,Int)=>Boolean): Unit = {
    K8055.getDevice(step.deviceId).map{
      sensor => sensor.analogueState.fold() {
        reading => step.value.fold() {
          target => if(compareFn(reading,target)) incStep()
        }
      }
    }
  }

  def runWaitOn(step: Step): Unit = {
    K8055.getDevice(step.deviceId).map{
      inputDevice => inputDevice.digitalState.fold() {
        isOn => if(isOn) incStep()
      }
    }
  }

  def runWaitOff(step: Step): Unit = {
    K8055.getDevice(step.deviceId).map{
      inputDevice => inputDevice.digitalState.fold() {
        isOn => if(!isOn) incStep()
      }
    }
  }


}
