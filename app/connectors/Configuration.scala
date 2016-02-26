package connectors

import play.api.Play

trait Configuration {
  val filename = Play.current.configuration.getString("file.name").fold("sequence.json") (filename => filename)
}

object Configuration extends Configuration
