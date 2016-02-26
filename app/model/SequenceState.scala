package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class SequenceState(id:String, digitalState:Option[Boolean]=None, analogueState:Option[Int]=None)

object SequenceState {
  implicit val sequenceStateReads: Reads[SequenceState] = (
  (JsPath \ "id").read[String] and
  (JsPath \ "digitalState").readNullable[Boolean] and
  (JsPath \ "analogueState").readNullable[Int]
  )(SequenceState.apply _)

  implicit val sequenceWrites = Json.writes[SequenceState]
}