package controllers

import connectors.{FakeK8055, Configuration, FakeSequenceConfigIO, SequenceConfigIO}
import model.{SequenceState$, Step$, Sequence$}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.{WithApplication}
import model.Step._
import manager.{SequenceManager, FakeMonitorManager}


@RunWith(classOf[JUnitRunner])
class DeviceCollectionControllerSpec extends Specification {

  object TestSequenceManager extends SequenceManager{
    override val deviceConfigIO = FakeSequenceConfigIO
    override val deviceManager = FakeDeviceManager
    override val monitorManager = FakeMonitorManager
    override val configuration = Configuration
    override val k8055Board = FakeK8055

    var latestDeviceCollection:Sequence = null
    override def putDeviceCollection(deviceCollection: Sequence):Boolean = {
      latestDeviceCollection = deviceCollection
      true
    }
  }

  "DeviceCollectionController" should {

    "retrieve a device collection" in new WithApplication{
      TestSequenceManager.getSequence.name must equalTo("Fake Name")
    }

    "populate devices with transient data" in new WithApplication {
      val populatedDc = TestSequenceManager.readAndPopulateDevices(FakeSequenceConfigIO.deviceCollection)
      populatedDc.devices.foreach(device =>

        device.deviceType match{
          case DIGITAL_OUT => device.id must equalTo("TEST-DO-1")
          case ANALOGUE_OUT => device.id must equalTo("TEST-AO-1")
          case DIGITAL_IN => device.id must equalTo("TEST-DI-1")
          case ANALOGUE_IN => device.id must equalTo("TEST-AI-1")
          case MONITOR => device.id must equalTo("TEST-MO-1")
        }
      )
    }

    "upsert a device in the device collection" in new WithApplication {
      val pump = Step("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      TestSequenceManager.upsertDevice(pump)
      val updatedPump:Option[Step] = TestSequenceManager.latestDeviceCollection.devices.find(d => d.id =="TEST-DO-1")
      val updatedDesc = updatedPump.fold("wrong!")(d=>d.description)
      updatedDesc must equalTo("updated-test-pump")
    }


    "update transient digital data for a digital out should succeed" in new WithApplication {
      val pump = Step("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      val result = TestSequenceManager.updateTransientDigitalOutData(pump)
      result must equalTo(true)
    }

    "update transient digital data for a monitor should succeed" in new WithApplication {
      val thermostat = Step("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), Some(0) )
      val result = TestSequenceManager.updateTransientDigitalOutData(thermostat)
      result must equalTo(true)
    }

    "update transient digital data for an analogue out should fail" in new WithApplication {
      val heater = Step("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
      val result = TestSequenceManager.updateTransientDigitalOutData(heater)
      result must equalTo(false)
    }

    "update transient digital data for an analogue in should fail" in new WithApplication {
      val thermometer = Step("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val result = TestSequenceManager.updateTransientDigitalOutData(thermometer)
      result must equalTo(false)
    }

    "update transient digital data for an digital in should fail" in new WithApplication {
      val switch = Step("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      val result = TestSequenceManager.updateTransientDigitalOutData(switch)
      result must equalTo(false)
    }


    "update transient analogue data for a digital out should fail" in new WithApplication {
      val pump = Step("TEST-DO-1", "updated-test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
      val result = TestSequenceManager.updateTransientAnalogueOutData(pump)
      result must equalTo(false)
    }

    "update transient analogue data for a monitor should succeed" in new WithApplication {
      val thermostat = Step("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), Some(0) )
      val result = TestSequenceManager.updateTransientAnalogueOutData(thermostat)
      result must equalTo(true)
    }

    "update transient analogue data for an analogue out should pass" in new WithApplication {
      val heater = Step("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
      val result = TestSequenceManager.updateTransientAnalogueOutData(heater)
      result must equalTo(true)
    }

    "update transient analogue data for an analogue in should fail" in new WithApplication {
      val thermometer = Step("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val result = TestSequenceManager.updateTransientAnalogueOutData(thermometer)
      result must equalTo(false)
    }

    "update transient analogue data for an digital in should fail" in new WithApplication {
      val switch = Step("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      val result = TestSequenceManager.updateTransientAnalogueOutData(switch)
      result must equalTo(false)
    }

    object TestSequenceManager2 extends SequenceManager{
      override val deviceConfigIO = FakeSequenceConfigIO
      override val deviceManager = FakeDeviceManager
      override val monitorManager = FakeMonitorManager
      override val configuration = Configuration
      override val k8055Board = FakeK8055

      override def updateTransientAnalogueOutData(device: Step) = {
        latestDevice = if(latestDevice == null) device
        else latestDevice.copy(analogueState = device.analogueState)
       true
      }
      override def updateTransientDigitalOutData(device: Step) = {
        latestDevice = if(latestDevice == null) device
          else latestDevice.copy(digitalState = device.digitalState)
        true
      }


      var latestDevice:Step = null
    }

    //Patch Devices
    "delta patch transient analogue out data should update the device correctly" in new WithApplication {
      val heaterState =  SequenceState("TEST-AO-1", None, Some(11))
      TestSequenceManager2.patchDevice(heaterState, true)
      val result = TestSequenceManager2.latestDevice.analogueState
      result must equalTo(Some(33))
    }

    "patch transient analogue out data should update the device correctly" in new WithApplication {
      val heaterState =  SequenceState("TEST-AO-1", None, Some(11))
      val succeeded = TestSequenceManager2.patchDevice(heaterState, false)
      succeeded must equalTo(true)
      val result = TestSequenceManager2.latestDevice.analogueState
      result must equalTo(Some(11))
    }

    "patch transient analogue in data should fail" in new WithApplication {
      val thermometerState =  SequenceState("TEST-AI-1", None, Some(11))
      val succeeded = TestSequenceManager2.patchDevice(thermometerState, false)
      succeeded must equalTo(false)
    }

    "patch transient digital in data should fail" in new WithApplication {
      val switchState =  SequenceState("TEST-DI-1", None, Some(11))
      val succeeded = TestSequenceManager2.patchDevice(switchState, false)
      succeeded must equalTo(false)
    }

    "patch transient digital out data should succeed" in new WithApplication {
      val pumpState =  SequenceState("TEST-DO-1", Some(true), None)
      val succeeded = TestSequenceManager2.patchDevice(pumpState, false)
      succeeded must equalTo(true)
      val result = TestSequenceManager2.latestDevice.digitalState
      result must equalTo(Some(true))
    }

    "delta patch transient analogue monitor data should update the device correctly" in new WithApplication {
      val thermometerState =  SequenceState("TEST-MO-1", None, Some(11))
      val succeeded = TestSequenceManager2.patchDevice(thermometerState, true)
      succeeded must equalTo(true)
      val aResult = TestSequenceManager2.latestDevice.analogueState
      aResult must equalTo(Some(12)) //FakeMonitorManager.getAnalogueOut returns 1, so 11+1=12
    }

    "patch transient analogue monitor data should update the device correctly" in new WithApplication {
      val thermostatState =  SequenceState("TEST-MO-1", None, Some(11))
      val succeeded = TestSequenceManager2.patchDevice(thermostatState, false)
      succeeded must equalTo(true)
      val aResult = TestSequenceManager2.latestDevice.analogueState
      aResult must equalTo(Some(11))
    }

    "patch transient digital monitor data should update the device correctly" in new WithApplication {
      val thermostatState =  SequenceState("TEST-MO-1", Some(true), None)
      TestSequenceManager2.patchDevice(thermostatState, false)
      val dResult = TestSequenceManager2.latestDevice.digitalState
      dResult must equalTo(Some(true))
    }

    //putDeviceCollection
    "almost pointless test, to ensure writeDeviceCollectionToFile is invoked" in new WithApplication {
      val dc = TestSequenceManager2.getSequence
      val result = TestSequenceManager2.putSequence(dc)
      result must equalTo(true)
    }


    object TestSequenceManager3 extends SequenceManager{
      override val deviceConfigIO = FakeSequenceConfigIO
      override val deviceManager = FakeDeviceManager
      override val monitorManager = FakeMonitorManager
      override val configuration = Configuration
      override val k8055Board = FakeK8055

      var latestDeviceCollection:Sequence = null

      override def putDeviceCollection(deviceCollection: Sequence):Boolean = {
        latestDeviceCollection = deviceCollection
        true
      }
    }

    //deleteDevice
    "delete device that exists should update the device collection correctly" in new WithApplication {
      val deviceId = "TEST-DI-1"
      val isInThere = TestSequenceManager3.getSequence.devices.find(device => device.id==deviceId)
      isInThere must not equals(None)
      val switch = Step(deviceId, "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
      TestSequenceManager3.deleteStep(switch)
      val dResult = TestSequenceManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }

    "delete device (by id) that exists should update the device collection correctly" in new WithApplication {
      val deviceId = "TEST-AI-1"
      val isInThere = TestSequenceManager3.getSequence.devices.find(device => device.id==deviceId)
      isInThere must not equals(None)
      TestSequenceManager3.deleteStep(deviceId)
      val dResult = TestSequenceManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }

    "handle a request to delete device that does not exist should leave device collection" in new WithApplication {
      val deviceId = "FAKE-AI-1"
      val isInThere = TestSequenceManager3.getSequence.devices.find(device => device.id==deviceId)
      isInThere must equalTo(None)
      val thermometer = Step(deviceId, "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
      val success = TestSequenceManager3.deleteStep(thermometer)
      success must equalTo(true)
      val dResult = TestSequenceManager3.latestDeviceCollection.devices.find(device => device.id==deviceId)
      dResult must equalTo(None)
    }
  }
}
