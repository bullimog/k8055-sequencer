package model

import play.api.libs.json.Json

case class ReadableStep(id:Int,
                        stepDescription:Option[String],
                        deviceId: String,
                        deviceDescription:String,
                        description: String,
                        value: Option[String])

object ReadableStep {
  implicit val readableStepReads = Json.reads[ReadableStep]
  implicit val readableStepWrites = Json.writes[ReadableStep]
}