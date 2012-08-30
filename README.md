Akka Emailer
============

This is a scala library for sending email. 

It uses the javax.mail package to send SMTP messages and optionally uses Akka to limit the rate of requests to the mail server. This can be useful when processing bulk mail, in the case that your app server issues requests faster than your mail server can handle them.

Configuration
-------------
Use application.conf, see the included example

Usage
------
val email = InternalEmailService.emailBuilder()
        .withTo("randyu@skechers.com")
        .withSubject("subject2")
        .withBody("Sent you an email")
        .withFormat("text/html")
        .build()

InternalEmailService().send(email)
