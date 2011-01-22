package com.verknowsys.served.spechelpers

import akka.actor._
import akka.actor.Actor._
import akka.dispatch._
import org.specs.Specification

import scala.collection.mutable.Queue

case class Expect(size: Int)

class SvdExpectActor extends Actor {
    val inbox = Queue[Any]()
    
    def receive = {
        case Expect(size) =>
            val future = self.senderFuture.get.asInstanceOf[CompletableFuture[Any]]
            if(inbox.size < size){
                become(expecting(future, size))
            } else {
                future.completeWithResult(take(size))
            }
            
        case msg => inbox enqueue msg
    }

    def expecting(future: CompletableFuture[Any], size: Int): Receive = {
        case msg =>
            inbox enqueue msg
            if(inbox.size >= size){
                future.completeWithResult(take(size))
                unbecome
        }
    }
    
    protected def take(size: Int) = (1 to size).foldLeft(List[Any]()){ case (xs, _) => xs :+ inbox.dequeue }
}

trait SvdExpectActorSpecification {
    self: Specification =>
    
    implicit def actorRef2exp(ref: ActorRef) = new {
        def ?(expected: Any*) = {
            expected.size match {
                case 1 if expected.head == nothing =>
                    (ref !! Expect(1)) match {
                        case Some(list: List[_]) => list must beEmpty
                        case None => isExpectation {} // HACK: scala-specs 'pending-as-failure' hack 
                    }
                    
                case _ =>
                    (ref !! Expect(expected.size)) match {
                        case Some(list: List[_]) => list mustEqual expected.toList
                        case None => fail("Actor timeout: Expected %s".format(expected.toList))
                    }
            }
        }
        
        def ?*(expected: Any*) = {
            (ref !! Expect(expected.size)) collect {
                case list: List[_] => list must containAll(expected.toSet)
                case None => fail("Actor timeout: Expected %s".format(expected.toSet))
            }
        }
    
    }
    
    val nothing = None
    
    var expectActor: ActorRef = null
    implicit var senderOption: Option[ActorRef] = None
    
    def beforeExpectActor {
        expectActor = actorOf[SvdExpectActor].start
        senderOption = Some(expectActor)
    }
    
    def afterExpectActor {
        expectActor.stop
    }
}
