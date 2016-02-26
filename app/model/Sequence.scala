package model

import model.Step._
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._


case class Sequence(name: String, description: String, steps: List[Step])

object Sequence{
  implicit val sequenceReads: Reads[Sequence] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "steps").read[List[Step]]
    )(Sequence.apply _)

  implicit val sequenceWrites = Json.writes[Sequence]

}
