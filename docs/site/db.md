---
title: Database
layout: default
---

Solution for persistence based on JVM oodb [NeoDatis](http://neodatis.org). It allows storing objects with full history of changes.

 
### Package and necessary imports

{% highlight scala %}
import com.verknowsys.served.db._
{% endhighlight %}


### Data model
Every object needs its own UUID


Create case class that extends `DBObject`
{% highlight scala %}
case class User(val name: String, id: UUID = randomUUID) extends DBObject(id)
{% endhighlight %}

Create collection object
{% highlight scala %}
object Users extends DB[User]
{% endhighlight %}


### Database connection

Only Client - Server mode is supported (on the same VM)

 
Start server. It requires `port` and `filepath`
{% highlight scala %}
val server = new DBServer(9000, "/path/to/datafile.db")
{% endhighlight %}

Open new client for that server. 
From now all operations will require `db` value
{% highlight scala %}
val db = server.openClient
{% endhighlight %}


### Insert

Create an object (UUID is automaticly generated with random value)
{% highlight scala %}
val user = User("teamon")
{% endhighlight %}

// Save object
{% highlight scala %}
db << user
{% endhighlight %}

// #### Insert many objects at once
{% highlight scala %}
val users = User("teamon") :: User("dmilith") :: User("lopex") :: Nil
db << users
{% endhighlight %}

### Update
 
Asuming `user` was saved, it will store new record in database with the same UUID and move existing one into history. 
Note that `newUser` and `user` are two different immutable objects. 
That's why every object stored in database has own UUID.

Update is albo possible for a list of objects (same API as insert)
{% highlight scala %}
val newUser = user.copy(name = "modified teamon")
db << newUser
{% endhighlight %}


### Select

#### Select by UUID

With some UUID given it is possible to select one record from database.
The result will be `Option[T]`
{% highlight scala %}
Users(db)(uuid)                 // => Some(User("teamon"))
Users(db)(someNotExistingUuid)  // => None
{% endhighlight %}

The `User(db)` method returns `DBCollection` object dedicated to `User` type objects.

#### Find records by query

`DBCollection` allows to find objects using pure scala syntax.
The `apply` method takes `T => Boolean` argument
{% highlight scala %}
Users(db)(_.name == "teamon")           // => List(User("teamon"))
Users(db)(_.name.indexOf("e") != -1)    // => List(User("teamon"), User("lopex"))
Users(db)(_.name == "nosuchuser")       // => Nil
{% endhighlight %}

`Users(db)` is already collection of all object of type `Uses`
{% highlight scala %}
Users(db)   // => List(User("teamon"), User("lopex"), User("dmilith"))
{% endhighlight %}

### History of changes

As mentioned above, object update does not modify already stored object but moves it to *history*.
To access history you need an object or its uuid.
The `historyFor` method returns `List[T]` of all previos versions of object, without current
in order from the newset to oldest.
{% highlight scala %}
val user = User("teamon")
db << user
db << user.copy(name = "teamon 2")
db << user.copy(name = "teamon 3")

Users(db).historyFor(user)  // => List(User("teamon 2"), User("teamon"))
{% endhighlight %}

Each object has a saving timestamp. You can access it with `savedAt` method.
{% highlight scala %}
user.savedAt // => Time of save action
{% endhighlight %}


The `Users` object is stateless and must be initialized with `db` argument.
Unline other database systems it has the save scope as `db` client.
To make life easier and more DRY it is possible to assign it to value.
{% highlight scala %}
val users = Users(db)
users(someUid)                  // => Some(...)
users.historyFor(otherUuid)     // => List(...)
{% endhighlight %}






