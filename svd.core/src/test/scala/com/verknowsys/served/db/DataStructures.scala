package com.verknowsys.served.db

case class User(val name: String, id: UUID = randomUUID) extends DBObject(id)
object Users extends DB[User]

case class Drug(val name: String, id: UUID = randomUUID) extends DBObject(id)
object Drugs extends DB[Drug]


class Item(val x: Int, val y: Int){
    override def equals(obj: Any) = obj match {
        case i: Item => x == i.x && y == i.y
        case _ => false
    }
}

case class EmbeddedList(val name: String, val list: List[Item], id: UUID = randomUUID) extends DBObject(id)
object EmbeddedList extends DB[EmbeddedList]
