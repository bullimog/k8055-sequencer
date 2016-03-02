package manager

import connectors.{Configuration, SequenceConfigIO, K8055}
import model.Step._
import model.{Step, Sequence, SequenceState}

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

}
