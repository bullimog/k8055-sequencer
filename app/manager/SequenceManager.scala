package manager

import connectors.{Configuration, SequenceConfigIO, K8055}
import model.Step._
import model.{Step, Sequence, SequenceState}

object SequenceManager extends SequenceManager with DeviceManager{
  override val sequenceConfigIO = SequenceConfigIO
  override val deviceManager = DeviceManager
  override val monitorManager = MonitorManager
  override val configuration = Configuration
  override val k8055Board = K8055
}

trait SequenceManager{

  val sequenceConfigIO:SequenceConfigIO
  val deviceManager:DeviceManager
  val monitorManager:MonitorManager
  val configuration:Configuration
  val k8055Board:K8055

  def getSequence:Sequence = {
    val oSequence: Option[Sequence] = sequenceConfigIO.readSequenceFromFile(configuration.filename)
    oSequence.fold(Sequence("No Sequence", "Error", List()))({
      sequence => sequence
    })
  }

  def getStep(stepId:String):Option[Step]={
    val sequence:Sequence = getSequence
    sequence.steps.find(step => step.id == stepId)
  }

  
  def readAndPopulateSteps(sequence: Sequence):Sequence = {
    val populatedSteps = sequence.steps.map(device =>
      device.deviceType match {
        case ANALOGUE_IN => deviceManager.readAndPopulateAnalogueIn(device)
        case ANALOGUE_OUT => deviceManager.readAndPopulateAnalogueOut(device)
        case DIGITAL_IN => deviceManager.readAndPopulateDigitalIn(device)
        case DIGITAL_OUT => deviceManager.readAndPopulateDigitalOut(device)
        case MONITOR => deviceManager.readAndPopulateMonitor(device)
        case _ => device
      }
    )
    sequence.copy(steps = populatedSteps)
  }


  def upsertDevice(step: Step):Boolean = {
    val sequence = getSequence
    val steps:List[Step] = sequence.steps
    val stepRemoved = steps.filter(d => d.id != step.id)
    val stepAdded = stepRemoved ::: List(step)
    val dc = sequence.copy(steps = stepAdded)

//    updateTransientDigitalOutData(step)
//    updateTransientAnalogueOutData(step)
    putSequence(dc)
  }


//  def updateTransientDigitalOutData(device: Step):Boolean = {
//    (device.deviceType, device.digitalState) match{
//      case (Step.DIGITAL_OUT, Some(dState)) => {
//        k8055Board.setDigitalOut(device.channel, dState)
//        true
//      }
//      case (Step.MONITOR, Some(dState)) => {
//        monitorManager.setDigitalOut(device.id, dState)
//        true
//      }
//      case _ => false
//    }
//  }

//  def updateTransientAnalogueOutData(device: Step):Boolean = {
//    (device.deviceType, device.analogueState) match{
//      case (Step.ANALOGUE_OUT, Some(aState)) => {
//        k8055Board.setAnalogueOut(device.channel, aState)
//        true
//      }
//      case (Step.MONITOR, Some(aState)) => {
//        monitorManager.setAnalogueOut(device.id, aState)
//        true
//      }
//      case _ => false
//    }
//  }



//  def patchDevice(deviceState: SequenceState, delta:Boolean):Boolean = {
//    val deviceCollection = getSequence
//    val devices:List[Step] = deviceCollection.devices
//
//    devices.find(d => d.id == deviceState.id).exists( device => {
//
//      device.deviceType match {
//        case MONITOR => {
//          val aState:Int = if (delta) {
//            val aRawState: Int = monitorManager.getAnalogueOut(device.id)
//            aRawState + deviceState.analogueState.getOrElse(0)
//          }
//          else
//            deviceState.analogueState.getOrElse(0)
//
//          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
//          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
//        }
//        case ANALOGUE_OUT => {
//          val aState = if (delta) {
//            val aRawState = k8055Board.getAnalogueOut(device.channel)
//            aRawState + deviceState.analogueState.getOrElse(0)
//          }
//          else
//            deviceState.analogueState.getOrElse(0)
//
//          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
//        }
//        case DIGITAL_OUT => updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
//        case _ => false
//      }
//    })
//  }


  def deleteStep(step: Step):Boolean = {deleteStep(step.id)}
  def deleteStep(step: String):Boolean = {
    val sequence = getSequence
    val steps:List[Step] = sequence.steps
    val stepRemoved = steps.filter(d => d.id != step)
    val sc = sequence.copy(steps = stepRemoved)
    putSequence(sc)
  }

  def putSequence(sequence: Sequence):Boolean = {
    sequenceConfigIO.writeSequenceToFile(configuration.filename, sequence)
  }

}
