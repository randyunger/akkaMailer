package email

import com.typesafe.config.{ConfigException, ConfigFactory}

/**
 * Created with IntelliJ IDEA.
 * User: randyu
 * Date: 8/30/12
 * Time: 11:43 AM
 */
object EmailConfiguration {
  private lazy val conf = ConfigFactory.load()

  private def loadConfigurationString(confKey: String):Option[String] = {
    val composedKey = confPrefix + confKey
    try {
      Some(conf.getString(composedKey))
    } catch {
      case e: ConfigException.Missing => {          //todo: Log this?
        None
      }
    }
  }

  val confPrefix = "sc-email."

  def apply(confKey: String):Option[String] = loadConfigurationString(confKey)

//  lazy val URL: String = loadConfigurationString("akka-couch.host")
}
