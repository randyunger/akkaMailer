package email.akkaEmailer

import email._
import email.EmailAttachment
import email.EmailHeaders
import email.InternalEmailService.Email
import email.InternalEmailService.InternalEmailException
import email.InternalEmailService.{InternalEmailException, Email}
import org.slf4j.LoggerFactory
import javax.mail.internet.{InternetAddress, MimeMultipart, MimeBodyPart, MimeMessage}
import javax.mail.{Transport, Message, Session}
import java.util.{Properties, Date}
import javax.activation.DataHandler
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import akka.actor.{Props, ActorSystem}
import javax.mail.util.ByteArrayDataSource
import scala.Some
import scala.Some
import scala.Some

/**
 * Created by IntelliJ IDEA.
 * User: randyu
 * Date: 1/31/12
 * Time: 12:26 PM
 */

case class AkkaEmailMessage(msg: Array[Byte], log: Boolean, exceptionList: List[Exception])

//use object instead of class so that ActorSystem handles are accessible across threads
object AkkaInternalEmailService extends InternalEmailServiceImpl {

  val akkaWorker = ActorSystem("AkkaInternalEmailService").actorOf(Props[AkkaInternalEmailActor].withDispatcher("mail-dispatcher"))
  val limiter = ActorSystem("AkkaInternalEmailService").actorOf(Props[AkkaInternalEmailLimiter].withDispatcher("mail-dispatcher"))

  override val logger = LoggerFactory.getLogger("AkkaInternalEmailService")

  override protected[email] def transmit(arrByte: Array[Byte], log: Boolean) {
    AkkaInternalEmailService.limiter ! AkkaEmailMessage(arrByte, log, Nil)
  }

  protected[email] def finalTransmit(arrByte: Array[Byte], log: Boolean) {
    super.transmit(arrByte, log)
  }
}