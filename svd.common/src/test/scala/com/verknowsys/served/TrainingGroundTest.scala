package com.verknowsys.served


import com.verknowsys.served.utils._
import com.verknowsys.served.testing._
// import scalaz._


class TrainingGroundTest extends DefaultTest with Logging {

    // import Scalaz._


    // trait M[A] {
    //     def m(a: A): Boolean
    // }

    // def toBool[A:M](a: A) = implicitly[M[A]].m(a)

    // implicit def mString: M[String] = new M[String] {
    //     def m(a: String) = a != ""
    // }

    // implicit def mInt: M[Int] = new M[Int] {
    //     def m(a: Int) = a != 0
    // }


    it should "understand implicits and Either with pattern matching and fold" in {
        implicit def convertIntToLeft(arg: Int) = Left(arg)
        implicit def convertStringToRight(arg: String) = Right(arg)

        def doSomething(something: Either[Int, String]) = something match {
            case Left(some: Int) =>
                Left(some + 10)
            case Right(some: String) =>
                Right(some + " is fun.")
        }

        def doSomethingFold(something: Either[Int, String]) = something.fold(
            some =>
                Left(some + 10),
            some =>
                Right(some + " is fun.")
        )

        doSomething(100) should be(Left(110))
        doSomething("this") should be(Right("this is fun."))
        doSomethingFold(100) should be(Left(110))
        doSomethingFold("this") should be(Right("this is fun."))


        type UUID = java.util.UUID
        implicit def convertUUIDtoString(uuid: UUID) = uuid.toString()

        val uuid = java.util.UUID.randomUUID()
        uuid.length should be(36)
    }

    it should "be able to use Either model for Exception handling" in {
        val a: Either[Throwable,Any] = Right("Nothing")

        def tryDangerous(some: => Any) = {
            try {
                Right(some)
            } catch {
                case e: Exception =>
                    Left(e)
            }
        }

        val exc = new Exception("Dangerous exception")
        def dangerousAction = {
            throw exc
        }
        def safeAction = {
            "something"
        }

        tryDangerous(dangerousAction) should be(Left(exc))
        tryDangerous(safeAction) should be(Right("something"))
    }


    it should "understand apply and unapply in type classes with implicit converters" in {
        type UUID = java.util.UUID

        trait ObjectToString[T] {
            def apply(t: T)
            def unapply(s: String): Option[T]
        }

        implicit val convertUUIDtoString: ObjectToString[UUID] = new ObjectToString[UUID] {
            def apply(a: UUID) = a.toString()
            def unapply(s: String) = try {
                    Some(java.util.UUID.fromString(s))
                } catch {
                    case e: Exception =>
                        None
                }
        }

        class MyTrainingGroundClass {
            val params: List[ObjectToString[UUID]] = Nil

        }



    }

    // it should "add entry" in {
    //     ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Trace)
    //     val res1 = ref !! Logger.ListEntries
    //     res1 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Trace))))
    //     TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Trace)

    //     ref !! Logger.AddEntry("com.verknowsys.served", Logger.Levels.Error)
    //     val res2 = ref !! Logger.ListEntries
    //     res2 should be(Some(Logger.Entries(Map("com.verknowsys.served" -> Logger.Levels.Error))))
    //     TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)

    //     ref !! Logger.AddEntry("com.verknowsys.served.foobar", Logger.Levels.Warn)
    //     val res3 = ref !! Logger.ListEntries
    //     res3 should be(Some(Logger.Entries(Map(
    //         "com.verknowsys.served" -> Logger.Levels.Error,
    //         "com.verknowsys.served.foobar" -> Logger.Levels.Warn
    //     ))))
    //     TestLogger.levelFor("com.verknowsys.served") should be(Logger.Levels.Error)
    //     TestLogger.levelFor("com.verknowsys.served.foobar") should be(Logger.Levels.Warn)

    //     ref.stop
    // }

    // it should "remove entry" in {
    //     ref !! Logger.AddEntry("com.verknowsys.served.a", Logger.Levels.Trace)
    //     ref !! Logger.AddEntry("com.verknowsys.served.b", Logger.Levels.Info)
    //     ref !! Logger.AddEntry("com.verknowsys.served.c", Logger.Levels.Error)
    //     ref !! Logger.RemoveEntry("com.verknowsys.served.b")
    //     val res1 = ref !! Logger.ListEntries
    //     res1 should be(Some(Logger.Entries(Map(
    //         "com.verknowsys.served.a" -> Logger.Levels.Trace,
    //         "com.verknowsys.served.c" -> Logger.Levels.Error
    //     ))))
    // }
}
