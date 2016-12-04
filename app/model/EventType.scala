package model

import utils.EnumUtils

object EventType extends Enumeration {
  type EventType = Value
  val ON,                     // turn on
      OFF,                    // turn off
      SET_VALUE,              // set analogue value (META-DATA = value[Int])
      WAIT_RISING,            // Wait-Target (META-DATA = target[Int])
      WAIT_FALLING,           // Wait-Target (META-DATA = target[Int])
      WAIT_TIME,              // Wait-Time   (META-DATA = duration[milliseconds])
      WAIT_ON,                // Wait until device is on
      WAIT_OFF,               // Wait until device is off
      WAIT_COUNT,             // Wait until a counter has reached n (META-DATA = target[Int])
      STROBE_ON_TIME,         // Set duration strobe will be on for
      STROBE_OFF_TIME = Value // Set duration strobe will be off for
  implicit val eventTypeFormat = EnumUtils.enumFormat(EventType)
}
