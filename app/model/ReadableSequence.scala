package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class ReadableSequence(description:String, readableSteps:List[ReadableStep])

object ReadableSequence {
  implicit val readableSequenceReads: Reads[ReadableSequence] = (
    (JsPath \ "description").read[String] and
    (JsPath \ "readableSteps").read[List[ReadableStep]]
   )(ReadableSequence.apply _)

  implicit val readableSequenceWrites = Json.writes[ReadableSequence]
}