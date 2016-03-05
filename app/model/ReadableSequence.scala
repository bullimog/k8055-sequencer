package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class ReadableSequence(description:String, readableSteps:List[ReadableStep], currentStep:Int, running:Boolean)

object ReadableSequence {
  implicit val readableSequenceReads: Reads[ReadableSequence] = (
    (JsPath \ "description").read[String] and
    (JsPath \ "readableSteps").read[List[ReadableStep]] and
    (JsPath \ "currentStep").read[Int] and
    (JsPath \ "running").read[Boolean]
   )(ReadableSequence.apply _)

  implicit val readableSequenceWrites = Json.writes[ReadableSequence]
}