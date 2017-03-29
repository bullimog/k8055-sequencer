package model

import play.api.libs.json.Json

case class Sequence(name: String, description: String, steps: List[Step])

object Sequence{
  implicit val sequenceReads = Json.reads[Sequence]
  implicit val sequenceWrites = Json.writes[Sequence]
}
