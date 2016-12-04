package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

//Used to post data to K8055
case class DeviceState(id:String, digitalState:Option[Boolean]=None, analogueState:Option[Int]=None,
                       strobeOnTime:Option[Int]=None, strobeOffTime:Option[Int]=None)

object DeviceState {
  implicit val deviceStateReads: Reads[DeviceState] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "digitalState").readNullable[Boolean] and
    (JsPath \ "analogueState").readNullable[Int] and
    (JsPath \ "strobeOnTime").readNullable[Int] and
    (JsPath \ "strobeOffTime").readNullable[Int]
  )(DeviceState.apply _)

  implicit val deviceWrites = Json.writes[DeviceState]
}