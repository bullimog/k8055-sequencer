package manager

import connectors.K8055
import model.Step


object DeviceManager extends DeviceManager{
  override val k8055Board = K8055
}

trait DeviceManager {
  val k8055Board:K8055


  def readAndPopulateAnalogueIn (device: Step):Step = {
    device.copy(analogueState = Some(k8055Board.getAnalogueIn(device.channel)))
  }

  def readAndPopulateAnalogueOut(device: Step):Step = {
    device.copy(analogueState = Some(k8055Board.getAnalogueOut(device.channel)))
  }

  def readAndPopulateDigitalIn(device: Step):Step = {
    device.copy(digitalState = Some(k8055Board.getDigitalIn(device.channel)))
  }

  def readAndPopulateDigitalOut(device: Step):Step = {
    device.copy(digitalState = Some(k8055Board.getDigitalOut(device.channel)))
  }

  def readAndPopulateMonitor(device: Step):Step = {
    device.copy(digitalState = Some(MonitorManager.getDigitalOut(device.id)),
      analogueState = Some(MonitorManager.getAnalogueOut(device.id)))
  }
}
