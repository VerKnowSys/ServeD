package com.verknowsys.served.db

case class User(val name: String, id: UUID = randomUUID) extends DBObject(id)
object Users extends DB[User]

case class Drug(val name: String, id: UUID = randomUUID) extends DBObject(id)
object Drugs extends DB[Drug]
