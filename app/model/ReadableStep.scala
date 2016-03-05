package model

import model.EventType._
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

case class ReadableStep(id:Int, deviceId: String, deviceDescription:String, description: String, value: Option[String])

object ReadableStep {
  implicit val readableStepReads: Reads[ReadableStep] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "deviceId").read[String] and
    (JsPath \ "deviceDescription").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "value").readNullable[String]
   )(ReadableStep.apply _)

  implicit val readableStepWrites = Json.writes[ReadableStep]
}