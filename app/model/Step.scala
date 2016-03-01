package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

import model.EventType.EventType

case class Step(id: Int, device: Int, eventType:EventType, target: Option[Double], duration: Option[Int]) {
  override def toString: String = {
    "##Step device:" + device + ", eventType:" + decode + ", target:" + target + ", duration:" + duration
  }
  def decode:String ={
    eventType match {
      case EventType.ON => "On"
      case EventType.OFF => "Off"
      case EventType.SET_VALUE => "Set to "
      case EventType.WAIT_RISING => "Wait until reading rises to "
      case EventType.WAIT_FALLING => "Wait until reading falls to "
      case EventType.WAIT_TIME => "Wait time for "
      case EventType.WAIT_ON => "Wait for on "
      case EventType.WAIT_OFF => "Wait for off "
      case EventType.WAIT_COUNT => "Wait for count"
    }
  }
}

object Step {
  implicit val stepReads: Reads[Step] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "device").read[Int] and
    (JsPath \ "eventType").read[EventType] and
    (JsPath \ "target").readNullable[Double] and
    (JsPath \ "duration").readNullable[Int]
   )(Step.apply _)

  implicit val stepWrites = Json.writes[Step]
}
