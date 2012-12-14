package com.verknowsys.served.notifications


import com.verknowsys.served._
import com.verknowsys.served.api._
import com.verknowsys.served.api.accountkeys._
import com.verknowsys.served.api.git._
import com.verknowsys.served.services._
import com.verknowsys.served.utils._

import org.jibble.pircbot._
import org.json4s._
import org.json4s.native._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import java.util.Calendar
import java.text.SimpleDateFormat
import redis.clients.jedis._
import scala.collection.JavaConverters._


/**
    @author tallica, dmilith
 */

class SvdIRCGate(account: SvdAccount) extends PircBot with Logging with SvdUtils with Gate {


    implicit val formats = DefaultFormats


    def settings = {
        setVerbose(false)
        setName("tasks-robo")
        setAutoNickChange(false)
        setVersion("0.1.0")
        setEncoding("UTF-8")
        try {
            connect("irc.freenode.net")
            joinChannel("#verknowsys")
        } catch {
            case e: NickAlreadyInUseException =>
                log.error("Can't connect to IRC. Nickname is already in use.")

            case e: Exception =>
                log.error("%s".format(e))

            disconnect
        }
    }


    def allowedUserNames = "dmilith" :: "tallica" :: "wick3d" :: Nil
    def tasksPerPage = 5
    def redisHost = SvdConfig.remoteApiServerHost
    def redisKey(nickname: String) = nickname + ".tasks"

    lazy val jedis = new Jedis(redisHost)


    abstract class TaskState
    case object open extends TaskState
    case object finished extends TaskState
    case object all extends TaskState


    case class Task(id: Int, content: String, date: Long, done: Boolean)
    case class Tasks(list: List[Task], nextId: Int)


    def connectToRedis = {
        log.info("Connecting to Redis server")
        try {
            // jedis.auth("password")
            jedis.connect
            true
        } catch {
            case e: Exception =>
                log.error(e.toString)
                false
        }
    }


    def disconnectFromRedis {
        log.info("Disconnecting from Redis server")
        jedis.disconnect
    }


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


    def getTasks(nickname: String, state: TaskState) =
        try {
            val json = parse(jedis.get(redisKey(nickname)))
            state match {
                case `finished` =>
                    parseTasks(json, true)
                case `open` =>
                    parseTasks(json, false)
                case _ =>
                    parseTasks(json)
            }
        } catch {
            case e: Exception =>
                log.error(e.toString)
                Tasks(list = Nil, nextId = 1)
        }


    def getAllTasks(nickname: String) = getTasks(nickname, all)
    def getFinishedTasks(nickname: String) = getTasks(nickname, finished)
    def getOpenTasks(nickname: String) = getTasks(nickname, open)


    def timeStamp = java.lang.System.currentTimeMillis / 1000L


    def setTasks(nickname: String, tasks: Tasks) = {
        val json = compact(render(renderTasks(tasks)))

        try {
            jedis.set(redisKey(nickname), json)
            true
        } catch {
            case e: Exception =>
                log.error(e.toString)
                false
        }
    }


    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {


        def listTasksCmd(nickname: String, state: TaskState) {
            if (allowedUserNames.contains(nickname)) {
                log.debug("Found allowed nickname: %s", nickname)
                val tasks = getTasks(nickname, state)

                if (tasks.list.length > 0) {
                    val count = "(%d of %d)".format(math.min(tasks.list.length, tasksPerPage), tasks.list.length)
                    val forWhom = if (sender != nickname) "for %s ".format(nickname) else ""
                    sendMessage(channel, "%s: Listing %s tasks %s%s.".format(sender, state, forWhom, count))
                    tasks.list.takeRight(tasksPerPage).map {
                        task =>
                            sendMessage(channel, "%s: #%d → %s".format(sender, task.id, task.content))
                    }
                } else {
                    val forWhom = if (sender != nickname) "for %s".format(nickname) else "sire"
                    sendMessage(channel, "%s: No %s tasks %s.".format(sender, state, forWhom))
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

                    setTasks(nickname, tasksUpdated) match {
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

                    setTasks(nickname, tasksUpdated) match {
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

                    setTasks(nickname, tasksUpdated) match {
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
                setTasks(nickname, Tasks(list = Nil, nextId = 1)) match {
                    case true =>
                        val append = if (sender != nickname) " for %s.".format(nickname) else "."
                        sendMessage(channel, "%s: Tasks wiped%s".format(sender, append))


                    case false =>
                        sendMessage(channel, "%s: Unfortunately, I can't wipe tasks. Please try again later.".format(sender))
                }
            else
                log.trace("Not allowed nickname: %s sending command: '%s'", nickname, message)
        }


        def termKey(term: String) = "term." + term.toLowerCase


        if (message.startsWith("\\"))
            message.substring(1).split(" ", 2).toList match {
                case term :: content :: Nil =>
                    jedis.rpush(termKey(term), content)
                    sendMessage(channel, "%s: Added a new item to term %s.".format(sender, term))

                case _ =>
                    sendMessage(channel, "%s: Wrong argument number.".format(sender))
            }


        if (message.startsWith("?"))
            message.substring(1).split(" ").toList match {
                case term :: Nil =>
                    val len = jedis.llen(termKey(term))

                    if (len > 0) {
                        val terms = jedis.lrange(termKey(term), 0, len)
                        terms.asScala.map {
                            content => sendMessage(channel, "%s: %s".format(sender, content))
                        }
                    }
                    else
                        sendMessage(channel, "%s: Term %s not found.".format(sender, term))

                case _ =>
                    sendMessage(channel, "%s: Wrong argument number.".format(sender))

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
                    listTasksCmd(sender, finished)

                case ".finished" :: nickname :: Nil =>
                    listTasksCmd(nickname, finished)

                case ".remove" :: ids =>
                    removeTasksCmd(sender, ids)

                case ".task" :: nickname :: content =>
                    addTaskCmd(nickname, content)

                case ".tasks" :: Nil =>
                    listTasksCmd(sender, open)

                case ".tasks" :: nickname :: Nil =>
                    listTasksCmd(nickname, open)

                case ".wipe" :: Nil =>
                    wipeTasksCmd(sender)

                case _ =>
            }
        }
    }


    def connect {
        log.info("Initiating SvdIRCGate")
        if (connectToRedis)
            settings
        else
            log.warn("Aborting startup duo to connection problems to Redis server")
    }


    def setStatus(st: String) {
    }


    def send(message: String) {

        // SvdNotifyMailer(message, SvdConfig.notificationMailRecipients)
    }

    override def onDisconnect {
        disconnectFromRedis
    }


}
