package connectors

object FakeK8055 extends K8055{

  override def executeCommand(command:String): String = {
    "10;20;30;40;50;60"
  }
}
