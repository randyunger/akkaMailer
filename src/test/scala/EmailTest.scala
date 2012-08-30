package email.src.test.scala

/**
 * Created with IntelliJ IDEA.
 * User: randyu
 * Date: 8/30/12
 * Time: 10:34 AM
 */


import email.{EmailConfiguration, EmailAttachment, InternalEmailService}
import org.specs2.mutable
import org.specs2.mutable._

class EmailTest extends Specification {

  "sc-email" should {
    "Build a new email with set params" in {
      val email = InternalEmailService.emailBuilder()
        .withFrom("from me")
        .withTo("to you")
        .withSubject("subject")
        .withBody("Hello world")
        .withAttachment(EmailAttachment("a", null, "b"))
        .withLog(true)
        .withFormat("text/plain")
        .build()

      email.toString must be equalTo "Email(from me,to you,subject,Hello world,text/plain,true,Some(EmailAttachment(a,null,b)),None)"
    }


    "Build a new email with defaults" in {
      val email = InternalEmailService.emailBuilder()
        .withTo("to you")
        .withSubject("subject")
        .withBody("Hello world")
        .build()

      email.toString must be equalTo "Email(info@skechers.com,to you,subject,Hello world,text/plain,false,None,None)"
    }

    "Send 2 test emails in an interval" in {
      val email = InternalEmailService.emailBuilder()
//        .withFrom("randyu@skechers.com")
        .withTo("randyu@skechers.com")
        .withSubject("subject2")
        .withBody("Sent you an email")
        .withFormat("text/html")
        .build()

      InternalEmailService().send(email)  //send twice to test interval
      InternalEmailService().send(email)

      val sleep = EmailConfiguration("akka-settings.minimum-interval").getOrElse("5").toInt
      Thread.sleep(sleep)         //if you don't sleep, specs2 will kill the akka threads and your second email won't be sent

      email.toString must be equalTo "Email(info@skechers.com,randyu@skechers.com,subject2,Sent you an email,text/html,false,None,None)"
    }
  }
}
