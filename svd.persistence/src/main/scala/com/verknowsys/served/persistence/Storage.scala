package com.verknowsys.served.persistence

import akka.actor.Actor
import akka.actor.Actor.actorOf
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.util._

case class AccountConfig(username: String, projects: List[ModuleConfig] = Nil)
case class ModuleConfig(name: String)

case class GetAccountConfig(username: String)

class Storage extends Actor {
    lazy val mongoConn = MongoConnection()
    lazy val mongoDB = mongoConn("served")

    // collections
    lazy val accounts = mongoDB("accounts")


    def receive = {
        case GetAccountConfig(username) =>
            self.reply(accounts.findOne(MongoDBObject("username" -> username)) match {
                case Some(ac) => grater[AccountConfig].asObject(ac)
                case None => AccountConfig(username)
            })
    }
}

object Storage {
    def apply() = instance
    lazy val instance = actorOf[Storage].start
}
