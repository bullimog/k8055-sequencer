package manager

import akka.actor.FSM.Failure
import connectors.{Configuration, SequenceConfigIO, K8055}
import model.Step._
import model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object SequenceManager extends SequenceManager{
  override val sequenceConfigIO = SequenceConfigIO
  override val configuration = Configuration
  override val k8055Board = K8055
}

trait SequenceManager{

  val sequenceConfigIO:SequenceConfigIO
  val configuration:Configuration
  val k8055Board:K8055


  def getSequence:Sequence = {
    val oSequence: Option[Sequence] = sequenceConfigIO.readSequenceFromFile(configuration.filename)
    oSequence.fold(Sequence("No Sequence", "Error", List()))({
      sequence => sequence
    })
  }

  def getReadableSequence:Future[ReadableSequence] = {
    sequenceToReadableSequence(getSequence)
  }

  def getStep(stepId:Int):Option[Step]={
    val sequence:Sequence = getSequence
    sequence.steps.find(step => step.id == stepId)
  }


  def upsertDevice(step: Step):Boolean = {
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
    for {device <- K8055.getDevice(step.deviceId)}
      yield{
        ReadableStep(step.id, step.deviceId, device.description , step.decode,
          step.value.map(v=>v.toString+device.units.getOrElse("")))
      }
  }

  //TODO: This is generic, could go in utils package?...
  private def listFuture2FutureList[T](lf: List[Future[T]]): Future[List[T]] =
    lf.foldRight(Future(Nil:List[T]))((list, listItem) =>
    for{
      _list <- list
      _listItem <- listItem
    } yield _list::_listItem)



  def sequenceToReadableSequence(sequence: Sequence):Future[ReadableSequence] = {
    val futureSteps:List[Future[ReadableStep]] =
      for (step <- sequence.steps)
      yield stepToReadableStep(step)

    listFuture2FutureList(futureSteps).map(steps=> ReadableSequence(sequence.description, steps,
      SequenceExecutionManager.currentStep, SequenceExecutionManager.running))
  }

}
