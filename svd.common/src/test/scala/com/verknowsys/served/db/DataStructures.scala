package com.verknowsys.served.db

case class User(val name: String, uuid: UUID = randomUUID) extends Persistent
object Users extends DB[User]

case class Drug(val name: String, uuid: UUID = randomUUID) extends Persistent
object Drugs extends DB[Drug]


class Item(val x: Int, val y: Int){
    override def equals(obj: Any) = obj match {
        case i: Item => x == i.x && y == i.y
        case _ => false
    }
}

case class EmbeddedList(val name: String, val list: List[Item], uuid: UUID = randomUUID) extends Persistent
object EmbeddedList extends DB[EmbeddedList]
