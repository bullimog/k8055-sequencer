package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

case class DeviceState(id:String, digitalState:Option[Boolean]=None, analogueState:Option[Int]=None)

object DeviceState {
  implicit val deviceStateReads: Reads[DeviceState] = (
  (JsPath \ "id").read[String] and
  (JsPath \ "digitalState").readNullable[Boolean] and
  (JsPath \ "analogueState").readNullable[Int]
  )(DeviceState.apply _)

  implicit val deviceWrites = Json.writes[DeviceState]
}