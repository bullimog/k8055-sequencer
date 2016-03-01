package controllers

import connectors.FakeK8055
import manager.DeviceManager
import model.Step


object FakeDeviceManager extends DeviceManager{

  override val k8055Board = FakeK8055

  override def readAndPopulateAnalogueIn (device: Step):Step = {
    device.copy(analogueState = Some(0))
  }

  override def readAndPopulateAnalogueOut(device: Step):Step = {
    device.copy(analogueState = Some(0))
  }

  override def readAndPopulateDigitalIn(device: Step):Step = {
    device.copy(digitalState = Some(false))
  }

  override def readAndPopulateDigitalOut(device: Step):Step = {
    device.copy(digitalState = Some(false))
  }

  override def readAndPopulateMonitor(device: Step):Step = {
    device.copy(digitalState = Some(false),
      analogueState = Some(0))
  }
}
