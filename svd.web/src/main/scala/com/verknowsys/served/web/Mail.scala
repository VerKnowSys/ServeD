package com.verknowsys.served.web


import net.liftweb.util.Mailer._
import net.liftweb.util.Props
import javax.mail.{Authenticator,PasswordAuthentication}


object Mail {


    def html(message: String) =
        <html>
            <head>
                <title>Hello { message } </title>
            </head>
            <body>
                <h1>Hello { message }</h1>
            </body>
        </html>


    def apply(message: String, recipient: String = "dmilith@gmail.com") = {
        authenticator = for {
            user <- Props.get("mail.user")
            pass <- Props.get("mail.password")
        } yield new Authenticator {

            override def getPasswordAuthentication =
                new PasswordAuthentication(user, pass)

        }

        sendMail(
            From("%s <%s>".format(Dict("WiatrSerwis Shop"), "wsklep@verknowsys.com")), // XXX: hardcoded
            Subject(Dict("Please confirm your order in our shop.")),
            To(recipient),
            html(message))

    }


}
