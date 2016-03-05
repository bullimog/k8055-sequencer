package model

import play.api.libs.functional.syntax._
import play.api.libs.json._
import model.EventType.EventType

case class Step(id: Int, deviceId: String, eventType:EventType, value: Option[Int]) {
  override def toString: String = {
    "##Step device:" + deviceId + ", eventType:" + decode + ", target:" + value
  }
  def decode:String ={
    eventType match {
      case EventType.ON => "Switch on"
      case EventType.OFF => "Switch off"
      case EventType.SET_VALUE => "Set to"
      case EventType.WAIT_RISING => "Wait until reading rises to"
      case EventType.WAIT_FALLING => "Wait until reading falls to"
      case EventType.WAIT_TIME => "Wait time for"
      case EventType.WAIT_ON => "Wait for on"
      case EventType.WAIT_OFF => "Wait for off"
      case EventType.WAIT_COUNT => "Wait for count"
    }
  }
}

object Step {
  implicit val stepReads: Reads[Step] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "device").read[String] and
    (JsPath \ "eventType").read[EventType] and
    (JsPath \ "value").readNullable[Int]
   )(Step.apply _)

  implicit val stepWrites = Json.writes[Step]
}
