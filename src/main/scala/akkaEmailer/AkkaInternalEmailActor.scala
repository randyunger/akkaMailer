package email.akkaEmailer

import akka.actor.Actor
import email.EmailConfiguration
import email.InternalEmailService.InternalEmailException

/**
 * Created by IntelliJ IDEA.
 * User: randyu
 * Date: 1/11/12
 * Time: 1:57 PM
 */

object AkkaInternalEmailActor {
  val numberOfRetries = EmailConfiguration("akka-settings.max-retries").getOrElse("5").toInt
  val minimumInterval = EmailConfiguration("akka-settings.minimum-interval").getOrElse("100").toInt
  val sleepFactor = 1.1

  var prevTimestamp: Long = 0
}

class AkkaInternalEmailActor extends Actor { //uses permanent storage of message queue - Durable Mailbox.
  def receive = {
    case AkkaEmailMessage(msg, log, exceptionList) => {
      if (exceptionList.length >= AkkaInternalEmailActor.numberOfRetries) {
        AkkaInternalEmailService.logger.error("Failed to send internal email after " + AkkaInternalEmailActor.numberOfRetries + " attempts." +
          "\n Message snippet: " + new String(msg).substring(0, 250))
        //        self.tryReply(exceptionList)
        throw new InternalEmailException(exceptionList.head)
      }
      else {
        try {
          AkkaInternalEmailService.logger.info("Sending message" + msg)
          AkkaInternalEmailService.finalTransmit(msg, log)
        }
        catch {
          case e: InternalEmailException => {
            AkkaInternalEmailService.logger.error("Exception caught, resending: " + msg, e)
            AkkaInternalEmailService.limiter !(msg, log, e :: exceptionList)
          }
        }
      }
    }
    case unknownMessage => {
      AkkaInternalEmailService.logger.error("Invalid message!", unknownMessage)
      AkkaInternalEmailService.limiter !(unknownMessage)         //worthwhile to resend? perhaps limiter can feed this back up the chain //todo
    }
  }
}

class AkkaInternalEmailLimiter extends Actor {
  def receive = {
    case AkkaEmailMessage(msg, log, exceptionList) => {
      val curr = new java.util.Date().getTime
      val elapsed = curr - AkkaInternalEmailActor.prevTimestamp
      if (elapsed < AkkaInternalEmailActor.minimumInterval) {
        val dur = ((AkkaInternalEmailActor.minimumInterval - elapsed) * AkkaInternalEmailActor.sleepFactor).toLong
        Thread.sleep(dur)
      }
      AkkaInternalEmailActor.prevTimestamp = new java.util.Date().getTime
      //      if(self.senderFuture.isDefined) self.reply(AkkaInternalEmailService.akkaWorker ? (msg, log, exceptionList))
      //      else
      AkkaInternalEmailService.akkaWorker ! AkkaEmailMessage(msg, log, exceptionList)
    }
  }
}