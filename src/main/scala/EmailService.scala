package email

import email.InternalEmailService.Email
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import email.akkaEmailer.AkkaInternalEmailService

/**
 * Created by IntelliJ IDEA.
 * User: randyu
 * Date: 1/28/12
 * Time: 11:58 AM
 */

case class EmailAttachment(fileName: String, file: ByteArrayOutputStream, contentType: String)

case class EmailHeaders(fname: String, lname: String)

//object ExternalEmailService {
//  def apply() = AkkaBlueHornetEmailService.asInstanceOf[ExternalEmailServiceIfc]
//}

object InternalEmailService {

  def apply(): InternalEmailServiceIfc = {
    EmailConfiguration("impl") match {
      case Some("InternalEmailServiceImpl") => new InternalEmailServiceImpl
      case _ => AkkaInternalEmailService
    }
  }

  case class InternalEmailException(e: Throwable) extends Exception(e)

  //Based on http://blog.rafaelferreira.net/2008/07/type-safe-builder-pattern-in-scala.html
  case class Email(from: String, to: String, subject: String, body: String, format: String, log: Boolean,
                   attachment: Option[EmailAttachment], headers: Option[EmailHeaders])

  abstract class TRUE

  //Abstract type parameters enforced later via implicit def [TRUE,TRUE,TRUE]
  abstract class FALSE

  //to ensure that withTo, withSubj, withBody must be called before build()

  class EmailBuilder[TO, SUBJECT, BODY](val from: Option[String], val to: Option[String], val subject: Option[String],
                                        val body: Option[String], val format: Option[String], val log: Option[Boolean],
                                        val attachment: Option[EmailAttachment], val headers: Option[EmailHeaders]) {
    def withFrom(f: String) = new EmailBuilder[TO, SUBJECT, BODY](Some(f), to, subject, body, format, log, attachment, headers)

    def withTo(t: String) = new EmailBuilder[TRUE, SUBJECT, BODY](from, Some(t), subject, body, format, log, attachment, headers)

    def withSubject(s: String) = new EmailBuilder[TO, TRUE, BODY](from, to, Some(s), body, format, log, attachment, headers)

    def withBody(b: String) = new EmailBuilder[TO, SUBJECT, TRUE](from, to, subject, Some(b), format, log, attachment, headers)

    def withFormat(fo: String) = new EmailBuilder[TO, SUBJECT, BODY](from, to, subject, body, Some(fo), log, attachment, headers)

    def withLog(l: Boolean) = new EmailBuilder[TO, SUBJECT, BODY](from, to, subject, body, format, Some(l), attachment, headers)

    def withAttachment(a: EmailAttachment) = new EmailBuilder[TO, SUBJECT, BODY](from, to, subject, body, format, log, Some(a), headers)

    def withHeaders(h: EmailHeaders) = new EmailBuilder[TO, SUBJECT, BODY](from, to, subject, body, format, log, attachment, Some(h))

//    @Deprecated //Do not use in scala - disables type-checking. Allows runtime error if mandatory fields not set
//    def buildJava(): Email = {
//      enableBuild(this.asInstanceOf[EmailBuilder[TRUE, TRUE, TRUE]]).build()
//    }
  }

  abstract case class EmailParams() {
    def build(): Email
  }

  implicit def enableBuild(builder: EmailBuilder[TRUE, TRUE, TRUE]) = new EmailParams {
    def build() = new Email(builder.from.getOrElse(EmailConfiguration("defaultFrom").getOrElse("")), //This is where the default values are set
                            builder.to.get,
                            builder.subject.get,
                            builder.body.get,
                            builder.format.getOrElse(EmailConfiguration("defaultFormat").getOrElse("text/plain")),
                            builder.log.getOrElse(false),
                            builder.attachment,
                            builder.headers)
  }

  def emailBuilder() = new EmailBuilder[FALSE, FALSE, FALSE](None, None, None, None, None, None, None, None)

}

//trait ExternalEmailServiceIfc {
//  def sendTestEmail(email: String, _type: String, data: java.util.List[_]): Object
//
//  def update(user: User): Object
//
//  def confirmUpdate(log: OrderLog): Object
//
//  def notify(email: String, sku: Sku, style: Style): Object
//
//  def subscribe(email: String): Object
//
//  def subscribeWithGrps(email: String, grps: String): Object
//
//  def confirmOrder(log: OrderLog): Object
//
//  def forgotPassword(user: User): Object
//
//  def sendEmail(user: User, subject: String, body: String, _type: String)
//
//  val logger = LoggerFactory.getLogger("ExternalEmailService")
//}

trait InternalEmailServiceIfc {
  def send(email: Email) //Please use this method whenever possible.

  def send(from: String, recpts: String, subject: String, body: String)

  @Deprecated
  def send(from: String, recpts: String, subject: String, body: String, format: String, log: Boolean, file: Option[EmailAttachment], hdrs: Option[EmailHeaders])

  val logger = LoggerFactory.getLogger("InternalEmailService")

  protected def HOST = EmailConfiguration("mailHost").getOrElse("localhost")

  protected lazy val TIMEOUT = new java.lang.Integer(10000)

//  def logMessage(message: MimeMessage, response: String) {//: EmailLog = {
//    val userId = UserService.getUser(message.getFrom.head.toString) match {
//      case Some(user: User) => user.getId
//      case _ => null
//    }
//    logMessage(Option(userId), message.getFrom.head.toString, message.getRecipients(Message.RecipientType.TO).head.toString, message.getSubject,
//      message.getContent.toString, response)
//  }

//  def logMessage(userId: Option[java.lang.Integer], from: String, recipients: String, subject: String, body: String, response: String = ""): EmailLog = {
//    // don't log exception emails
//    if (recipients == EJBConstants.EXCEPTION_EMAIL_TO && (subject == EJBConstants.EXCEPTION_SUBJECT || subject == EJBConstants.ADMIN_EXCEPTION_SUBJECT)) {
//      return null
//    }
//
//    val emailLog: EmailLog = new EmailLog()
//    userId.foreach(u => emailLog.setUserId(u))
//    emailLog.setSender(from.toUpperCase)
//    emailLog.setRecipients(recipients.toUpperCase)
//    emailLog.setSubject(subject)
//    emailLog.setBody(body)
//    emailLog.setTimestamp(Calendar.getInstance())
//    emailLog.setResponse(response)
//    PersistenceContext().persist(emailLog)
//    emailLog
//  }
}