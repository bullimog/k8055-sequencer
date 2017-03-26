package model

import play.api.libs.json.Json


case class Program(name: String, description: String, sequenceFiles: List[String]) {
}

object Program {
  implicit val programReads = Json.reads[Program]
  implicit val programWrites = Json.writes[Program]
}