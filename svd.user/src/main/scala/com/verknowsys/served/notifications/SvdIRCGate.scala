package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.utils._

import org.jibble.pircbot._
import scala.io._
import org.json4s._
import org.json4s.native._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import java.util.Calendar
import java.text.SimpleDateFormat


/**
    @author tallica, dmilith
 */

class SvdIRCGate(account: SvdAccount) extends PircBot with Logging with SvdUtils with Gate {


    implicit val formats = DefaultFormats


    def settings = {
        setVerbose(false)
        setName("tasks-robo")
        setAutoNickChange(true)
        setVersion("0.1.0")
        setEncoding("UTF-8")
        connect("irc.freenode.net")
        joinChannel("#verknowsys")
    }


    def allowedUserNames = "dmilith" :: "tallica" :: "wick3d" :: Nil
    def tasksPerPage = 5


    def tasksFile(nickname: String) = {
        val path = SvdConfig.userHomeDir / "%d/%s.tasks".format(account.uid, nickname)
        log.debug("Looking for tasks file at: %s".format(path))
        path
    }


    case class Task(id: Int, content: String, date: Long, done: Boolean)
    case class Tasks(list: List[Task], nextId: Int)


    def renderTasks(tasks: Tasks) = (
        ("list" -> tasks.list.map {
            task => (
                ("id" -> task.id) ~
                ("content" -> task.content) ~
                ("date" -> task.date) ~
                ("done" -> task.done)
            )
        }) ~
        ("nextId" -> tasks.nextId)
    )


    def parseTask(task: JValue) =
        Task(
            id = (task \ "id").extract[Int],
            content = (task \ "content").extract[String],
            date = (task \ "date").extract[Long],
            done = (task \ "done").extract[Boolean]
        )


    def parseTasks(tasks: JValue) =
        Tasks(
            list = (tasks \ "list").children.map {
                task => parseTask(task)
            },
            nextId = (tasks \ "nextId").extract[Int]
        )


    def parseTasks(tasks: JValue, finished: Boolean) =
        Tasks(
            list = (tasks \ "list").children.filter {
                task => ((task \ "done").extract[Boolean] == finished)
            }.map {
                task => parseTask(task)
            },
            nextId = (tasks \ "nextId").extract[Int]
        )


    def getTasks(nickname: String, tasksType: String) =
        try {
            val json = parse(Source.fromFile(tasksFile(nickname)).mkString)
            tasksType match {
                case "finished" =>
                    parseTasks(json, true)
                case "open" =>
                    parseTasks(json, false)
                case _ =>
                    parseTasks(json)
            }
        } catch {
            case e: Exception =>
                log.error(e.toString)
                Tasks(list = Nil, nextId = 1)
        }


    def getAllTasks(nickname: String) = getTasks(nickname, "all")
    def getFinishedTasks(nickname: String) = getTasks(nickname, "finished")
    def getOpenTasks(nickname: String) = getTasks(nickname, "open")


    def timeStamp = java.lang.System.currentTimeMillis / 1000L


    def writeTasksToFile(nickname: String, tasks: Tasks) = {
        val json = compact(render(renderTasks(tasks)))

        try {
            writeToFile(tasksFile(nickname), json)
            true
        } catch {
            case e: Exception =>
                log.error(e.toString)
                false
        }
    }


    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {


        def listTasksCmd(nickname: String, tasksType: String) {
            if (allowedUserNames.contains(nickname)) {
                log.debug("Found allowed nickname: %s", nickname)
                val tasks = getTasks(nickname, tasksType)

                if (tasks.list.length > 0) {
                    val count = "(%d of %d)".format(math.min(tasks.list.length, tasksPerPage), tasks.list.length)
                    val forWhom = if (sender != nickname) "for %s ".format(nickname) else ""
                    sendMessage(channel, "%s: Listing %s tasks %s%s.".format(sender, tasksType, forWhom, count))
                    tasks.list.takeRight(tasksPerPage).map {
                        task =>
                            sendMessage(channel, "%s: #%d → %s".format(sender, task.id, task.content))
                    }
                } else {
                    val forWhom = if (sender != nickname) "for %s".format(nickname) else "sire"
                    sendMessage(channel, "%s: No %s tasks %s.".format(sender, tasksType, forWhom))
                }
            } else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        def addTaskCmd(nickname: String, content: List[String]) {
            if (allowedUserNames.contains(nickname)) {
                log.debug("Found allowed nickname: %s", nickname)
                if (content.length > 0) {
                    val tasks = getAllTasks(nickname)
                    val newTask = Task(tasks.nextId, content.mkString(" "), timeStamp, false)
                    val tasksUpdated = Tasks(tasks.list ::: List(newTask), tasks.nextId + 1)

                    writeTasksToFile(nickname, tasksUpdated) match {
                        case true =>
                            val append = if (sender != nickname) " for %s.".format(nickname) else "."
                            sendMessage(channel, "%s: Added a new task #%d%s".format(sender, tasks.nextId, append))

                        case false =>
                           sendMessage(channel, "%s: Unfortunately, I can't add task to the list. Please try again later.".format(sender))
                    }
                } else
                    sendMessage(channel, "%s: You need to specify task's content.".format(sender))
            } else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        def doneTasksCmd(nickname: String, ids: List[String]) {
            if (allowedUserNames.contains(nickname)) {
                log.debug("Found allowed nickname: %s", nickname)
                if (ids.length > 0) {
                    val tasks = getAllTasks(nickname)
                    val tasksIds = ids.map { _.toInt }
                    val tasksList = tasks.list.map {
                        task =>
                            if (tasksIds.contains(task.id))
                                Task(task.id, task.content, task.date, true)
                            else
                                task
                    }
                    val tasksUpdated = Tasks(tasksList, tasks.nextId)

                    writeTasksToFile(nickname, tasksUpdated) match {
                        case true =>
                            val append = if (sender != nickname) " for %s.".format(nickname) else "."
                            val plural = if (tasksIds.length > 1) "s" else ""
                            sendMessage(channel, "%s: Task%s %s marked as done%s".format(sender, plural, tasksIds.map("#" + _).mkString(", "), append))

                        case false =>
                            sendMessage(channel, "%s: Unfortunately, I can't mark tasks as done. Please try again later.".format(sender))
                    }
                } else
                    sendMessage(channel, "%s: You need to specify at least one task's ID.".format(sender))
            } else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        def removeTasksCmd(nickname: String, ids: List[String]) {
            if (allowedUserNames.contains(nickname)) {
                log.debug("Found allowed nickname: %s", nickname)
                if (ids.length > 0) {
                    val tasks = getAllTasks(nickname)
                    val tasksIds = ids.map { _.toInt }
                    val tasksList = tasks.list.filterNot {
                        task => tasksIds.contains(task.id)
                    }
                    val tasksUpdated = Tasks(tasksList, tasks.nextId)

                    writeTasksToFile(nickname, tasksUpdated) match {
                        case true =>
                            val append = if (sender != nickname) " for %s.".format(nickname) else "."
                            sendMessage(channel, "%s: Tasks (%s) removed%s".format(sender, tasksIds.map("#" + _).mkString(", "), append))

                        case false =>
                            sendMessage(channel, "%s: Unfortunately, I can't remove tasks. Please try again later.".format(sender))
                    }
                } else
                    sendMessage(channel, "%s: You need to specify at least one task's ID.".format(sender))
            } else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        def wipeTasksCmd(nickname: String) {
            if (allowedUserNames.contains(nickname))
                writeTasksToFile(nickname, Tasks(list = Nil, nextId = 1)) match {
                    case true =>
                        val append = if (sender != nickname) " for %s.".format(nickname) else "."
                        sendMessage(channel, "%s: Tasks wiped%s".format(sender, append))


                    case false =>
                        sendMessage(channel, "%s: Unfortunately, I can't wipe tasks. Please try again later.".format(sender))
                }
            else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        if (message.startsWith(".")) {
            message.split(" ").toList match {
                case ".help" :: Nil =>
                    sendMessage(channel, "%s: Available commands: .(add|done|finished|ping|remove|task|tasks|wipe)".format(sender))

                case ".ping" :: Nil =>
                    log.debug("Received ping request from: %s", sender)
                    sendMessage(channel, "%s: pong".format(sender))

                case ".add" :: content =>
                    addTaskCmd(sender, content)

                case ".done" :: ids =>
                    doneTasksCmd(sender, ids)

                case ".finished" :: Nil =>
                    listTasksCmd(sender, "finished")

                case ".finished" :: nickname :: Nil =>
                    listTasksCmd(nickname, "finished")

                case ".remove" :: ids =>
                    removeTasksCmd(sender, ids)

                case ".task" :: nickname :: content =>
                    addTaskCmd(nickname, content)

                case ".tasks" :: Nil =>
                    listTasksCmd(sender, "open")

                case ".tasks" :: nickname :: Nil =>
                    listTasksCmd(nickname, "open")

                case ".wipe" :: Nil =>
                    wipeTasksCmd(sender)

                case _ =>
            }
        }
    }


    def connect {
        log.info("Initiating SvdIRCGate")
        settings
    }


    def setStatus(st: String) {
    }


    def send(message: String) {

        // SvdNotifyMailer(message, SvdConfig.notificationMailRecipients)
    }


}
