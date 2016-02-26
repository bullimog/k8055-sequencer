package manager

import akka.actor.{Props, ActorRef, ActorSystem}
import controllers.FakeSequenceManager
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.mock._
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class MonitorActorSpec extends Specification{

  object TestSequencerActor extends SequencerActorTrait{
    override val sequencerManager = FakeSequenceManager
  }


  "MonitorActor" should{
    "result in no change, when there are no active Monitors" in new WithApplication{
      FakeSequenceManager.heaterAnalogueState = 0
      FakeSequenceManager.thermometerAnalogueState = 0
      FakeSequenceManager.thermostatAnalogueState = 0
      FakeSequenceManager.thermostatDigitalState = false
      TestSequencerActor.processActiveMonitors()
      FakeSequenceManager.lastDeviceState must equalTo(null)
    }


    "Not change anything, when there is one active Monitor which is on target" in new WithApplication{
      FakeSequenceManager.heaterAnalogueState = 0
      FakeSequenceManager.thermometerAnalogueState = 2000
      FakeSequenceManager.thermostatAnalogueState = 2000
      FakeSequenceManager.thermostatDigitalState = true
      TestSequencerActor.processActiveMonitors()
      FakeSequenceManager.lastDeviceState.analogueState must equalTo(Some(0))
    }

    "Set Monitor increaser, when there is one active Monitor which is below target" in new WithApplication{
      FakeSequenceManager.heaterAnalogueState = 0
      FakeSequenceManager.thermometerAnalogueState = 2000
      FakeSequenceManager.thermostatAnalogueState = 2001
      FakeSequenceManager.thermostatDigitalState = true
      TestSequencerActor.processActiveMonitors()
      FakeSequenceManager.lastDeviceState.analogueState must equalTo(Some(40))
    }

    "Switch off Monitor increaser, when there is an active Monitor which is above target" in new WithApplication {
      FakeSequenceManager.heaterAnalogueState = 0
      FakeSequenceManager.thermometerAnalogueState = 2000
      FakeSequenceManager.thermostatAnalogueState = 1999
      FakeSequenceManager.thermostatDigitalState = true
      TestSequencerActor.processActiveMonitors()
      FakeSequenceManager.lastDeviceState.analogueState must equalTo(Some(0))
    }


    "calculate the output setting properly when it is a lot above target " in {
      val result = TestSequencerActor.calculateOutputSetting(8)
      result must equalTo(255)
    }
    "calculate the output setting properly when it is above target " in {
      val result = TestSequencerActor.calculateOutputSetting(3)
      result must equalTo(120)
    }

    "calculate the output setting properly when there is a small drift from target " in {
      val result = TestSequencerActor.calculateOutputSetting(2)
      result must equalTo(80)
    }

    "calculate the output setting properly when there is a tiny drift from target " in {
      val result = TestSequencerActor.calculateOutputSetting(1)
      result must equalTo(40)
    }

    "calculate the output setting properly when it is on target " in {
      val result = TestSequencerActor.calculateOutputSetting(0)
      result must equalTo(0)
    }

    "calculate the output setting properly when it is below target " in {
      val result = TestSequencerActor.calculateOutputSetting(-1)
      result must equalTo(0)
    }


  }
}
