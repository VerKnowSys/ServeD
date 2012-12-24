package com.verknowsys.served.utils


// import net.liftweb.util.Mailer._
// import net.liftweb.util.Props
// import javax.mail.{Authenticator,PasswordAuthentication}


/**
 *  @author dmilith
 *  Fast implementation of Mailer to be used with notification system.
 */
object SvdNotifyMailer extends Logging {


    // private def html(message: String) =
    //     <html>
    //         <head>
    //             <title>Hello { message } </title>
    //         </head>
    //         <body>
    //             <h1>Hello { message }</h1>
    //         </body>
    //     </html>


    // def apply(message: String, recipients: List[String] = SvdConfig.notificationMailRecipients) = {
    //     authenticator = for {
    //         user <- Some(SvdConfig.notificationMailUser)
    //         pass <- Some(SvdConfig.notificationMailPassword)
    //     } yield new Authenticator {

    //         override def getPasswordAuthentication =
    //             new PasswordAuthentication(user, pass)

    //     }
    //     recipients.map {
    //         recip =>
    //             sendMail(
    //                 From("%s <%s>".format(Dict("VerKnowSys"), "notifications@verknowsys.com")), // XXX: hardcoded
    //                 Subject(Dict("ServeD Account Notification Center message.")),
    //                 To(recip),
    //                 html(message))
    //     }


    // }


}
