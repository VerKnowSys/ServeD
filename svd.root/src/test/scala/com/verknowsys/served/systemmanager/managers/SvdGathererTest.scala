///*
// * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
// * This Software is a close code project. You may not redistribute this code without permission of author.
// */
//
//package com.verknowsys.served.systemmanager.managers
//
//
//import com.verknowsys.served.utils._
//import com.verknowsys.served.api._
//import com.verknowsys.served.api.Admin._
//
//import java.util.{Calendar, GregorianCalendar}
//import akka.pattern.ask
//import akka.testkit.TestKit
//import akka.actor.{ActorSystem, Props}
//import com.verknowsys.served.testing._
//
//
//class SvdGathererTest(_system: ActorSystem) extends TestKit(_system) with DefaultTest with Logging  with SvdUtils {
//
//    def this() = this(ActorSystem("svd-test-system"))
//
//    val homeDir1 = randomPath //testPath("home/teamon")
//    val homeDir2 = randomPath //testPath("home/dmilith")
//
//    val account1 = new SvdAccount(userName = "teamon", uid = randomPort)
//    val account2 = new SvdAccount(userName = "dmilith", uid = randomPort)
//
//    var gather1: ActorRef = null
//    var gather2: ActorRef = null
//
//
//    override def beforeAll {
//        gather1 = system.actorOf(Props(new SvdGatherer(account1)))
//        gather2 = system.actorOf(Props(new SvdGatherer(account2)))
//        mkdir(homeDir1)
//        mkdir(homeDir2)
//    }
//
//    override def afterAll {
//        rmdir(homeDir1)
//        rmdir(homeDir2)
//        system.shutdown
//    }
//
//
//    it should "create more than one instance of SvdGatherer" in {
//        (gather1 ? ("Test signal 1")) onSuccess {
//            case Error(y) =>
//                (gather2 ? Init) onSuccess {
//                    case ApiSuccess =>
//                        true must be(true)
//
//                    case x =>
//                        fail("Problem: %s".format(x))
//                }
//
//            case x =>
//                fail("Problem: %s".format(x))
//        }
//    }
//
//
//    it should "Calendar should give correct values" in {
//        val calendar0 = new GregorianCalendar(0,0,0,0,0,0)
//        calendar0.get(Calendar.HOUR) must be(0)
//        calendar0.get(Calendar.MINUTE) must be(0)
//        calendar0.get(Calendar.SECOND) must be(0)
//
//        val calendar1 = new GregorianCalendar(0,0,0,0,0,0)
//        calendar1.set(Calendar.SECOND, 3666)
//        calendar1.get(Calendar.HOUR) must be(1)
//        calendar1.get(Calendar.MINUTE) must be(1)
//        calendar1.get(Calendar.SECOND) must be(6)
//
//        val calendar2 = new GregorianCalendar(0,0,0,0,0,0)
//        calendar2.set(Calendar.SECOND, 3667)
//        calendar2.get(Calendar.HOUR) must be(1)
//        calendar2.get(Calendar.MINUTE) must be(1)
//        calendar2.get(Calendar.SECOND) must be(7)
//    }
//
//
//    it should "secondsToHMS() should give correct values" in {
//        val matcher = secondsToHMS(3666)
//        matcher must be("01h:01m:06s")
//        val matcher2 = secondsToHMS(3667L.toInt)
//        matcher2 must be("01h:01m:07s")
//    }
//
//
//    it should "Gather JSON from usage sys (executed on FreeBSD hosts only)" in {
//        if (isBSD)
//            (gather1 ? GetSysUsage(501)) onSuccess {
//                case Some(x: String) =>
//                    log.info("GATHERED: %s".format(x))
//
//                case None =>
//                    fail("None? Wtf?")
//            }
//    }
//
//        // "we should be able to check when it's worth to compress String" in {
//        //     val in = new BufferedReader(new FileReader("/dev/urandom"))
//        //
//        //     val str = new StringBuilder("")
//        //     println(bench {
//        //         for (i <- 1.to(150)) {
//        //             str.append(in.read)
//        //         }
//        //     })
//        //
//        //     val chpoint = str.toString
//        //     // println("str: %s".format(chpoint))
//        //     val chplen = chpoint.length
//        //     val complen = compress(chpoint).length
//        //     val decomplen = decompress(compress(chpoint)).length
//        //     println("chpoint (length): %d".format(chplen))
//        //     println("chpoint (compress): %d".format(complen))
//        //     println("chpoint (decompress): %d".format(decomplen))
//        //     for (i <- 1.to(5000)) {
//        //         str.append(in.read)
//        //     }
//        //     println("Will compress String of length: %d".format(str.toString.length))
//        //     println("####################################################################\n" +
//        //         bench {
//        //             decompress(compress(str.toString))
//        //         }
//        //     )
//        //     in.close
//        //     chplen must beEqual(decomplen)
//        //     chplen must beGreaterThan(complen)
//        // }
//
//}
