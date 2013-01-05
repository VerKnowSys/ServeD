/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

// package com.verknowsys.served.utils

// import akka.actor.ActorRef

// /**
//  * Handle global reference to actor
//  *
//  * Usage:
//  *    object MyGlobal extends GlobalActorRef(Actor.registry.actorFor[MyActor])
//  *    object MyGlobal extends GlobalActorRef(Some(Actor.remove.actorFor[MyActor]))
//  *
//  * @author teamon
//  */
// abstract class GlobalActorRef(actorFun: => ActorRef) {
//     lazy val actor = actorFun

//     def apply() = actor

//     // HACK: This should be donw with implicit conversion
//     def !(message: Any)(implicit sender: Option[ActorRef] = None) = actor ! message
//     def !!(message: Any)(implicit sender: Option[ActorRef] = None) = actor !! message
// }

// abstract class OptionalGlobalActorRef(actorFun: => Option[ActorRef]) {
//     lazy val actor = actorFun

//     def apply() = actor

//     def !(message: Any)(implicit sender: Option[ActorRef] = None) = actor.foreach { _ ! message }
//     def !!(message: Any)(implicit sender: Option[ActorRef] = None) = actor.foreach { _ !! message }
// }

