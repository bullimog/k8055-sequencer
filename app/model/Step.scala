package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Reads}


case class Step(id: Int, device: Int, eventType:Int, target: Option[Double], duration: Option[Int]) {
  override def toString: String = {
    "##Step device:" + device + ", eventType:" + decode + ", target:" + target + ", duration:" + duration
  }
  def decode:String ={
    eventType match {
      case Step.ON => "On"
      case Step.OFF => "Off"
      case Step.SET_TARGET => "Set to "
      case Step.WAIT_RISING => "Wait until reading rises to "
      case Step.WAIT_TIME => "Wait for "
      case Step.WAIT_ON => "Wait for "
    }
  }
}

object Step {
  //  Step(event) Types.
  val ON = 1            //  1 = turn on
  val OFF = 2           //  2 = turn off
  val SET_TARGET = 3    //  3 = set monitor (META-DATA = target[Double])
  val WAIT_RISING = 4   //  4 = Wait-Target (META-DATA = target[Double])
  val WAIT_TIME = 5     //  5 = Wait-Time   (META-DATA = duration[milliseconds])
  val WAIT_FALLING = 6  //  6 = Wait-Target (META-DATA = target[Double])
  val WAIT_ON = 7       //  7 = Wait until device is on
  val WAIT_OFF = 7      //  8 = Wait until device is off
  val WAIT_COUNT = 8    //  9 = Wait until a counter has reached n (META-DATA = target[Int])

  implicit val stepReads: Reads[Step] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "device").read[Int] and
    (JsPath \ "eventType").read[Int] and
    (JsPath \ "target").readNullable[Double] and
    (JsPath \ "duration").readNullable[Int]
   )(Step.apply _)

  implicit val stepWrites = Json.writes[Step]

}
