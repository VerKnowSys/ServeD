package com.verknowsys.served.spechelpers

import akka.actor._
import akka.actor.Actor._
import akka.dispatch._
import org.specs.Specification

import scala.collection.mutable.Queue

case class Expect(size: Int)

class Expector extends Actor {
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
    
    protected def take(size: Int) = {
        val x = inbox.take(3).toList //(1 to size).foldLeft(List[Any]()){ case (xs, _) => xs :+ inbox.dequeue }
        println(x)
        println(x.size)
        x
    }
}

trait ExpectActor {
    self: Specification =>
    
    implicit def actorRef2exp(ref: ActorRef) = new {
        def ?(expected: Any*) = {
            (ref !! Expect(expected.size)) collect {
                case list: List[_] => list mustEqual expected.toList
                case None => throw new RuntimeException("TIMEOUT")
            }
        }
        
        def ?*(expected: Any*) = {
            (ref !! Expect(expected.size)) collect {
                case list: List[_] => list must containAll(expected.toSet)
                case None => throw new RuntimeException("TIMEOUT")
            }
        }
    }
    
    var expectActor: ActorRef = null
    implicit var senderOption: Option[ActorRef] = None
    
    doBeforeSpec {
        expectActor = actorOf[Expector].start
        senderOption = Some(expectActor)
    }
    
    doAfterSpec {
        expectActor.stop
    }
}
