/**
 * Created with IntelliJ IDEA.
 * User: randyu
 * Date: 8/30/12
 * Time: 2:21 PM
 */
package email

import email.InternalEmailService.{InternalEmailException, Email}
import org.slf4j.LoggerFactory
import javax.mail.internet.{InternetAddress, MimeMultipart, MimeBodyPart, MimeMessage}
import javax.mail.{Transport, Message, Session}
import java.util.{Properties, Date}
import javax.activation.DataHandler
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.mail.util.ByteArrayDataSource


class InternalEmailServiceImpl extends InternalEmailServiceIfc {

  override val logger = LoggerFactory.getLogger("InternalEmailServiceImpl")

  val props = new Properties
  props.put("mail.smtp.host", HOST)
  props.put("mail.smtp.connectiontimeout", TIMEOUT)
  props.put("mail.smtp.timeout", TIMEOUT)

  def send(from: String, recpts: String, subject: String, body: String) {
    send(from, recpts, subject, body, "text/plain", false, None, None)
  }

  def send(email: Email) {
    send(email.from, email.to, email.subject, email.body, email.format, email.log, email.attachment, email.headers)
  }

  //Copied from existing code. todo:improve this?
  def send(from: String, recpts: String, subject: String, body: String, format: String, log: Boolean = false,
           file: Option[EmailAttachment] = None, hdrs: Option[EmailHeaders] = None) {
    val session = Session.getInstance(props)
    val msg = new MimeMessage(session)

    msg.setFrom(new InternetAddress(from))
    msg.setRecipients(Message.RecipientType.TO, recpts)
    //    msg.setSubject(subject)
    //    msg.setContent(body, format)
    // Force content format type to be UTF-8
    msg.setSubject(subject, "UTF-8")
    msg.setContent(body, format + "; charset=" + "UTF-8")
    msg.setSentDate(new Date())

    file match {
      case Some(EmailAttachment(fileName: String, file: ByteArrayOutputStream, contentType: String)) => {
        val mbp1: MimeBodyPart = new MimeBodyPart()
        mbp1.setText(body, "UTF-8")
        //        mbp1.setContent(body, format)
        // Force content format type to be UTF-8
        mbp1.setContent(body, format + "; charset=" + "UTF-8")

        // set attachment
        val bds: ByteArrayDataSource = new ByteArrayDataSource(file.toByteArray, contentType)
        val mbp2: MimeBodyPart = new MimeBodyPart()
        mbp2.setDataHandler(new DataHandler(bds))
        mbp2.setFileName(fileName)

        val mp: MimeMultipart = new MimeMultipart()
        mp.addBodyPart(mbp1)
        mp.addBodyPart(mbp2)

        msg.setContent(mp)
      }
      case _ => {}
    }

    hdrs match {
      case Some(EmailHeaders(fname: String, lname: String)) => {
        //set headers for instant service
        msg.setHeader("X-IS-CUSTFIRSTNAME", fname)
        msg.setHeader("X-IS-CUSTLASTNAME", lname)
        msg.setHeader("X-IS-CUSTEMAIL", from)
      }
      case _ => {}
    }
    //       Option("a","b") match { case Some((x:String,y:String)) => println(x+"$"+y) ; case _ => {}}
    //todo: enable logging
    //    if (log) logMessage(msg, "") //Log before sending message, while PersistenceContext is still accessible

    //manually deserialize content to outputstream (via writeto), send to akka, and reconstitute via constructor to MimeMessage
    val os = new ByteArrayOutputStream()
    msg.writeTo(os)
    transmit(os.toByteArray, log)
  }

  protected[email] def transmit(ba: Array[Byte], log: Boolean) {
    val message = new MimeMessage(Session.getInstance(props), new ByteArrayInputStream(ba))
    if (false) {         //todo: Perhaps read from app.conf?
      logger.warn("Not sending email for test server")
      return
    }
    logger.debug("Sending message: " + message)
    try {
      Transport.send(message)
    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        throw new InternalEmailException(ex)
      }
    }
  }
}
