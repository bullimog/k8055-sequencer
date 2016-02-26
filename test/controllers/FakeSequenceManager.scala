package controllers

import manager.SequenceManager
import model.Step._
import model.{SequenceState$, Step$, Sequence$}

object FakeSequenceManager extends SequenceManager{
  override val deviceConfigIO = null
  override val deviceManager = null
  override val monitorManager = null
  override val configuration = null
  override val k8055Board = null

  var thermostatDigitalState = false
  var thermostatAnalogueState:Int = 0
  var heaterAnalogueState:Int = 0
  var coolerAnalogueState:Int = 0
  var thermometerAnalogueState:Int = 0

  var lastDeviceState: SequenceState = null

  override def getSequence:Sequence ={
    val pump = Step("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
    val heater = Step("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0),
      analogueState = Some(heaterAnalogueState))
    val switch = Step("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
    val thermometer = Step("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0),
      analogueState = Some(thermometerAnalogueState))
    val thermostat = Step("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"),
      Some("TEST-AO-1"), Some("TEST-AO-2"), Some(thermostatDigitalState), Some(thermostatAnalogueState) )
    val fakeDevices:List[Step] = List(pump, heater, switch, thermometer, thermostat)
    Sequence("Fake Name", "Fake Description", fakeDevices)
  }

  override def readAndPopulateDevices(deviceCollection: Sequence):Sequence ={
    getSequence
  }

  override def patchDevice(deviceState: SequenceState, delta:Boolean):Boolean = {
    lastDeviceState = deviceState
    true
  }
}
