package manager

import connectors.{Configuration, SequenceConfigIO, K8055}
import model._
import utils.ListUtils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SequenceManager extends SequenceManager{
  override val sequenceConfigIO = SequenceConfigIO
  override val configuration = Configuration
  override val sequenceExecutionManager = SequenceExecutionManager
  override val k8055Board = K8055
  override val timer = Timer
}

trait SequenceManager{

  val sequenceConfigIO:SequenceConfigIO
  val configuration:Configuration
  val k8055Board:K8055
  val sequenceExecutionManager : SequenceExecutionManager
  val timer:Timer


  def getSequence:Sequence = {
    val oSequence: Option[Sequence] = sequenceConfigIO.readProgramFromFile(configuration.filename)
    oSequence.fold(Sequence("No Sequence", "Error", List()))({
      sequence => sequence
    })
  }

  def getReadableSequence:Future[ReadableSequence] = {
    sequenceToReadableSequence(getSequence)
  }

  def getSequenceState:SequenceState = {
    SequenceState(sequenceExecutionManager.running, sequenceExecutionManager.currentStep, timer.remainingTime())
  }

  def getStep(stepId:Int):Option[Step]={
    val sequence:Sequence = getSequence
    sequence.steps.find(step => step.id == stepId)
  }


  def upsertStep(step: Step):Boolean = {
    val sequence = getSequence
    val steps:List[Step] = sequence.steps
    val stepRemoved = steps.filter(d => d.id != step.id)
    val stepAdded = stepRemoved ::: List(step)
    val dc = sequence.copy(steps = stepAdded)

    putSequence(dc)
  }


  def deleteStep(step: Step):Boolean = {deleteStep(step.id)}
  def deleteStep(step: Int):Boolean = {
    val sequence = getSequence
    val steps:List[Step] = sequence.steps
    val stepRemoved = steps.filter(d => d.id != step)
    val sc = sequence.copy(steps = stepRemoved)
    putSequence(sc)
  }

  def putSequence(sequence: Sequence):Boolean = {
    sequenceConfigIO.writeSequenceToFile(configuration.filename, sequence)
  }

  private def stepToReadableStep(step: Step):Future[ReadableStep] = {
    if(step.eventType == EventType.WAIT_TIME){
      Future(ReadableStep(step.id, step.stepDescription, step.deviceId, "Timer", step.decode, Some(formatTimer(step.value))))
    }
    else {
      for {device <- K8055.getDevice(step.deviceId)}
        yield {ReadableStep(step.id, step.stepDescription, step.deviceId, device.description, step.decode, formatValue(device, step))
      }
    }
  }

  private def formatValue(device: Device, step: Step):Option[String] = {
    step.value.map { value =>
      val unRounded: Double = device.conversionFactor.getOrElse(1.0) * value +
        device.conversionOffset.getOrElse(0.0)

      val roundFactor: Double = math.pow(10.0, device.decimalPlaces.getOrElse(0).toDouble)
      val roundedValue: Double = math.round(unRounded * roundFactor) / roundFactor
      roundedValue.toString + device.units.fold("")(u=>u)
    }
  }

  def formatTimer(totalSeconds:Option[Int]):String = {
    val hours:Int = math.floor(totalSeconds.getOrElse(0) / 3600).toInt
    val ts:Int = totalSeconds.getOrElse(0) % 3600
    val mins:Int = Math.floor(ts / 60).toInt
    val secs:Int = totalSeconds.getOrElse(0) % 60
    padZero(hours)+":"+padZero(mins)+":"+padZero(secs)
  }

  def padZero(num:Int):String = {
    if(num<10) "0"+num
    else num.toString
  }

  def sequenceToReadableSequence(sequence: Sequence):Future[ReadableSequence] = {
    val futureSteps:List[Future[ReadableStep]] =
      for (step <- sequence.steps)
      yield stepToReadableStep(step)

    ListUtils.listOfFutures2FutureList(futureSteps).map(steps=> ReadableSequence(sequence.description, steps))
  }

}
