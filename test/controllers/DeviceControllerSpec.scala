/**
Copyright © 2016 Graeme Bullimore

This file is part of BulliBrew.
BulliBrew is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BulliBrew is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
  along with BulliBrew.  If not, see <http://www.gnu.org/licenses/>.
  */
package controllers

import connectors.FakeK8055
import manager.DeviceManager
import model.Step._
import model.Step$
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class DeviceControllerSpec extends Specification{

  object TestDeviceManager extends DeviceManager{
    override val k8055Board = FakeK8055
  }

  "DeviceController" should {

    "populate the analogue in for the device" in new WithApplication {
      val thermometer = Step("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, digitalState = None)
      val changedThermometer:Step = TestDeviceManager.readAndPopulateAnalogueIn(thermometer)
      changedThermometer.analogueState must equalTo(Some(30))
    }

    "populate the analogue out for the device" in new WithApplication {
      FakeK8055.analogueOut1 = 2600
      val heater = Step("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, digitalState = None)
      val changedHeater:Step = TestDeviceManager.readAndPopulateAnalogueOut(heater)
      changedHeater.analogueState must equalTo(Some(26))
    }

    "populate the digital out for the device" in new WithApplication {
      FakeK8055.digitalOut = 3
      val pump = Step("TEST-DO-1", "test-pump", DIGITAL_OUT, channel = 1, analogueState = None, digitalState = Some(false))
      val changedPump:Step = TestDeviceManager.readAndPopulateDigitalOut(pump)
      changedPump.digitalState must equalTo(Some(true))

      FakeK8055.digitalOut = 6
      val pump2 = Step("TEST-DO-1", "test-pump", DIGITAL_OUT, channel = 1, analogueState = None, digitalState = Some(true))
      val changedPump2:Step = TestDeviceManager.readAndPopulateDigitalOut(pump)
      changedPump2.digitalState must equalTo(Some(false))
    }

    "populate the digital in for the device" in new WithApplication {
      val switch = Step("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(true))
      val changedSwitch:Step = TestDeviceManager.readAndPopulateDigitalIn(switch)
      changedSwitch.digitalState must equalTo(Some(false))
    }

  }
}
