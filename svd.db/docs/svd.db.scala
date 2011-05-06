/**
 * **svd.db** is **ServeD** solution for persistence based on JVM oodb [NeoDatis][neodatis]
 * 
 * It allows storing objects with full history of changes.
 * 
 * 
 * [neodatis]: http://neodatis.org
 */
 
// ### Package and necessary imports

 
import com.verknowsys.served.db._
 

// ### Data model
//
// Every object needs its own UUID


// Create case class that extends `DBObject`
case class User(val name: String, id: UUID = randomUUID) extends DBObject(id)

// Create collection object (it *should* be plural form of above)
object Users extends DBCollection[User]


// ### Database connection
//
// Only Client - Server mode is supported (on the same VM)

 
// Start server. It requires `port` and `filepath`
val server = new DBServer(9000, "/path/to/datafile.db")

// Open new client for that server. 
// From now all operations will require `db` value
val db = server.openClient


// ### Insert

// Create an object (UUID is automaticly generated with random value)
val user = User("teamon")

// Save object
db << user

// #### Insert many objects at once
val users = User("teamon") :: User("dmilith") :: User("lopex") :: Nil
db << users


// ### Update
 
// Asuming `user` was saved, it will store new record in database with the same UUID and move existing one into history. 
// Note that `newUser` and `user` are two different immutable objects. 
// That's why every object stored in database has own UUID.
//
// Update is albo possible for a list of objects (same API as insert)
val newUser = user.copy(name = "modified teamon")
db << newUser


// ### Select

// #### Select by UUID

// Given some UUID it is possible to select one record from database
// The result will be `Option[T]`
Users(db)(uuid)                 // => Some(User("teamon"))
Users(db)(someNotExistingUuid)  // => None

// The `User(db)` method returns `DBCollection` object dedicated to `User` type objects

// #### Find records by query

// `DBCollection` allows to find objects using pure scala syntax
// The `apply` method takes `T => Boolean` argument
Users(db)(_.name == "teamon")           // => List(User("teamon"))
Users(db)(_.name.indexOf("e") != -1)    // => List(User("teamon"), User("lopex"))
Users(db)(_.name == "nosuchuser")       // => Nil

// To get all recors form database simply call `all`
Users(db).all   // => List(User("teamon"), User("lopex"), User("dmilith"))

// ### History of changes

// As mentioned above, object update does not modify already stored object but moves it to *history*
// To access history you need an object or its uuid
// The `historyFor` method returns `List[T]` of all previos versions of object, without current
// in order from the newset to oldest
val user = User("teamon")
db << user
db << user.copy(name = "teamon 2")
db << user.copy(name = "teamon 3")

Users(db).historyFor(user)  // => List(User("teamon 2"), User("teamon"))

// Each object has a saving timestamp. You can access it with `savedAt` method
user.savedAt // => Time of save action


// The `Users` object is stateless and must be initialized with `db` argument.
// Unline other database systems it has the save scope as `db` client.
// To make life easier and more DRY it is possible to assign it to value.
val users = Users(db)
users(someUid)                  // => Some(...)
users.historyFor(otherUuid)     // => List(...)



/**
 * 
 * **svd.db** is part of **ServeD** project. 
 * 
 * © Copyright 2009-2011 VerKnowSys
 *
 * Daniel Dettlaff and Tymon Tobolski
 * 
 * This Software is a close code project. You may not redistribute this code or documentation without permission of author.
 * 
 */


