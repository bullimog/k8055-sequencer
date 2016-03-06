package model

import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class SequenceState(running:Boolean, currentStep:Int, timeRemaining:String)

object SequenceState {
  implicit val sequenceStateReads: Reads[SequenceState] = (
  (JsPath \ "running").read[Boolean] and
  (JsPath \ "currentStep").read[Int] and
    (JsPath \ "timeRemaining").read[String]
  )(SequenceState.apply _)

  implicit val sequenceWrites = Json.writes[SequenceState]
}