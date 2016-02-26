package connectors

import java.io.{File, PrintWriter, FileNotFoundException}
import model.Sequence
import play.Logger
import play.api.libs.json.{Json, JsError, JsSuccess, JsValue}
import scala.io.Source

object SequenceConfigIO extends SequenceConfigIO
trait SequenceConfigIO {

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

}
