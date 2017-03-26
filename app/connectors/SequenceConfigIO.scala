package connectors

import java.io.{File, FileNotFoundException, PrintWriter}

import model.{Program, Sequence, Step}
import play.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.io.Source

object SequenceConfigIO extends SequenceConfigIO


trait SequenceConfigIO {
  val emptySequence = Sequence("NoName", "NoDescription", List())

  def parseSequence(json: JsValue):Option[Sequence] = {
    json.validate[Sequence] match {
      case s: JsSuccess[Sequence] => Some(s.get)
      case e: JsError => None
    }
  }

  def readSequenceFromFile(fileName:String):Option[Sequence] = {
    try{
      val source = Source.fromFile(fileName, "UTF-8")
      val json: JsValue = Json.parse(source.mkString)
      parseSequence(json)
    }catch{
      case e:FileNotFoundException => None
    }
  }

  def writeSequenceToFile(fileName: String, deviceCollection: Sequence):Boolean = {
    try{
      val writer = new PrintWriter(new File(fileName))
      writer.write(Json.prettyPrint(Json.toJson(deviceCollection)))
      writer.close()
      true
    }
    catch{
      case e: Exception => Logger.error("Could not write to file: "+e); false
    }
  }

  def parseProgram(json: JsValue):Option[Program] = {
    json.validate[Program] match {
      case s: JsSuccess[Program] => Some(s.get)
      case e: JsError => None
    }
  }

  def readAllSequencesInProgram(oProgram: Option[Program]):Option[List[Step]] = {
    oProgram.map{ program => program.sequenceFiles.map{
      seqFileName => readSequenceFromFile(seqFileName).fold(emptySequence)(sequence => sequence)
    }}.map(sequences => sequences.flatMap(sequence => sequence.steps))
  }

  def reindexSteps(unindexedSteps: Option[List[Step]]):Option[List[Step]] = {
    unindexedSteps.map{allStepsInAllSequences =>
      allStepsInAllSequences.zipWithIndex.map(stepWithIndex => stepWithIndex._1.copy(id = stepWithIndex._2+1))}
  }

  def readProgramFromFile(progFileName:String):Option[Sequence] = {
    try{
      val source = Source.fromFile(progFileName, "UTF-8")
      val json: JsValue = Json.parse(source.mkString)
      val oProgram = parseProgram(json)

      val oAllStepsInAllSequences = readAllSequencesInProgram(oProgram)
      val oReIndexedAllStepsInAllSequences = reindexSteps(oAllStepsInAllSequences)

      oProgram.flatMap { program =>
        oReIndexedAllStepsInAllSequences.map{ lStep =>
          Sequence(program.name, program.description, lStep)
        }
      }
    }catch{
      case e:FileNotFoundException => {
        println("File Not found: " + e)
        None
      }
    }
  }

}
