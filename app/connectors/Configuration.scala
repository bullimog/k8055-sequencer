package connectors

import play.api.Play

trait Configuration {
  val filename = Play.current.configuration.getString("file.name").fold("sequence.json") (filename => filename)
  val k8055Host = Play.current.configuration.getString("k8055.host").fold("badHostConfig") (filename => filename)
  val k8055Devices = Play.current.configuration.getString("k8055.devicespath").fold("badDevicesConfig") (filename => filename)
  val k8055DeviceState = Play.current.configuration.getString("k8055.deviceState").fold("badDeviceStateConfig") (filename => filename)
  val k8055Device = Play.current.configuration.getString("k8055.devicepath").fold("badDeviceStateConfig") (filename => filename)
}

object Configuration extends Configuration
