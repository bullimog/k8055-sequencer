package connectors

import model.Step._
import model.{Step$, Sequence$}


object FakeSequenceConfigIO extends SequenceConfigIO{

  val pump = Step("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
  val heater = Step("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
  val switch = Step("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
  val thermometer = Step("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
  val thermostat = Step("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), Some(0) )
  val fakeDevices:List[Step] = List(pump, heater, switch, thermometer, thermostat)
  val deviceCollection:Sequence = Sequence("Fake Name", "Fake Description", fakeDevices)


  override def readSequenceFromFile(fileName:String):Option[Sequence] = {
    Some(deviceCollection)
  }

  override def writeSequenceToFile(fileName: String, deviceCollection: Sequence):Boolean = {
    true
  }

}
