/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils


import com.verknowsys.served.utils.signals._
import com.verknowsys.served._
import SvdPOSIX._

import java.security.MessageDigest
import org.apache.commons.io.FileUtils
import clime.messadmin.providers.sizeof.ObjectProfiler
import scala.collection.JavaConversions._
import java.io._
import java.net._
import java.util.UUID
import java.util.{Calendar, GregorianCalendar}
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater
import sun.misc.SignalHandler
import sun.misc.Signal


/**
 * SvdUtils trait containing common functions
 *
 * @author dmilith
 *
 */
trait SvdUtils extends Logging {

    final lazy val clib = CLibrary.instance
    final lazy val cstat = CStat.instance


    /**
     *  Simple helper function to generate SHA1 hash from String.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def sha1(input: String) = {
        val mDigest = MessageDigest.getInstance("SHA1")
        val result = mDigest.digest(input.getBytes)
        val sb = new StringBuffer
        for (i <- 0 to result.length - 1) {
            sb.append(Integer.toString((result(i) & 0xff) + 0x100, 16).substring(1))
        }
        sb.toString
    }


    /**
      * A way to get default shell path depended on currently running operating system
      *
      * @author dmilith
      *
      */
    def defaultShell = {
        System.getProperty("os.name") match {
            case "FreeBSD" | "Mac OS X" =>
                "/Software/Zsh/exports/zsh"

            case _ =>
                "/usr/bin/zsh"

        }
    }


    /**
      * Kills system process with given pid and signal
      *
      * @author dmilith
      *
      * @return true if succeeded, false if failed
      *
      */
    def kill(pid: Long, signal: SvdPOSIX.Value = SIGINT) = {
        log.trace("Sending %d signal to pid %d".format(signal.id, pid))
        if (clib.kill(pid, signal.id) == 0)
            true
        else
            false
    }


    /**
     * Changes owner of file at given path
     *
     * @author dmilith
     */
    def chown(path: String, user: Int, group: Int = SvdConfig.defaultUserGroup, recursive: Boolean = true) =
        if (!(new File(path)).exists) {
            log.warn("Chown: File/ path doesn't exists! Cannot chown non existant file/ directory! Ignoring!")
            false
        } else {

            val files =
                if (recursive) recursiveListFilesFromPath(new File(path))
                    else List(
                        new File(path))

            files.par.map {
                file =>
                    if (clib.chown(file.getAbsolutePath, user, group) != 0)
                        log.warn("Chown failed on file: %s. Ignoring", file)
            }
            true
        }


    /**
     * Changes permissions of file at given path
     *
     * @author dmilith
     */
    def chmod(path: String, mode: Int, recursive: Boolean = true) =
        if (!(new File(path)).exists) {
            log.warn("Chmod: File or directory doesn't exists! Cannot chmod non existant file: '%s'! IGNORING!".format(path))
            false
        } else {
            val files = if (recursive) recursiveListFilesFromPath(new File(path))else List(new File(path))
            log.trace("chmod(path: %s, mode: %d, recursion: %s)".format(path, mode, recursive))

            for (file <- files) {
                log.trace("chmoding: %s".format(file.getAbsolutePath))
                if (clib.chmod(file.getAbsolutePath, mode) != 0)
                    log.error("Couldn't chmod file: %s. Check file access?", file)
                    // throwException[Exception]("Error occured while chmoding: %s".format(file))
            }
            true
        }


    /**
     *  Returns real host name (not "localhost")
     *  @author dmilith
     */
    def currentHost = InetAddress.getLocalHost


    /**
     *  Get ServeD VPN local IP address.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def currentVPNHost = InetAddress.getAllByName(currentHost.getHostName).map{_.getAddress.mkString(".")}.find{
            _.matches(SvdConfig.defaultVPNNetworkPrefix)
        } getOrElse currentHost.getAddress.mkString(".")


    /**
     *  Formats message for Notification System
     *  @author dmilith
     */
    def formatMessage(msg: String) = {
        val current = currentVPNHost
        msg(0) match { // first char of message
            case 'W' | 'w' =>
                "WARN -- %s -- %s".format(current, msg.substring(2))
            case 'E' | 'e' =>
                "ERROR == %s == %s".format(current, msg.substring(2))
            case 'F' | 'f' =>
                "FATAL ## %s ## %s".format(current, msg.substring(2))
            case 'D' | 'd' =>
                "DEBUG -- %s -- %s".format(current, msg.substring(2))
            case 'I' | 'i' =>
                "INFO -- %s -- %s".format(current, msg.substring(2))
            case _ =>
                "INFO -- %s -- %s".format(current, msg.substring(2))
        }
    }

    /**
        Unified & DRY method of throwing exceptions
        @author dmilith
    */
    def throwException[T <: Throwable : Manifest](message: String) {
        val exception = implicitly[Manifest[T]].erasure.getConstructor(classOf[String]).newInstance(message).asInstanceOf[T]
        // log.error("Error occured in %s.\nException: %s\n\n%s".format(this.getClass.getName, exception, exception.getStackTrace.mkString("\n")))
        throw exception
    }


    /**
    *   check and inform when current user isn't superuser (root)
    *
    *   @author dmilith
    */
    def rootCheck {
        System.getProperty("user.name") match {
            case "root" =>
            case _ =>
                lazy val err = "%s must be run as root user to perform some operations!".format(this.getClass)
                sys.error(err)
        }
    }


    /**
     *   returns true if running system matches BSD
     *
     *  @author dmilith
     */
    def isBSD = System.getProperty("os.name").contains("BSD")


    /**
     *   returns true if running system matches Darwin
     *
     *  @author dmilith
     */
    def isOSX = System.getProperty("os.name").contains("Darwin")


    /**
     *   returns true if running system matches Linux
     *
     *  @author dmilith
     */
    def isLinux = System.getProperty("os.name").contains("Linux")


    /**
     *   Generate unique identifier
     *
     *  @author dmilith
     */
    def newUuid = UUID.randomUUID


    /**
     *   Returns uid owner of given file/dir
     *
     *  @author dmilith
     */
     def getOwner(path: String) = {
        cstat.getOwner(path)
     }


    /**
     *  Checks and creates (if missing) given directory name
     *
     *  @author dmilith
     */
    def checkOrCreateDir(dir: String) = {
        if (new File(dir).exists) {
            log.debug("Directory: '%s' exists".format(dir))
        } else {
            log.debug("No directory named: '%s' available! Creating empty one.".format(dir))
            new File(dir).mkdirs
        }
        dir
    }


    /**
     *   simple String compression (zip inflate/deflate)
     *
     *  @author dmilith
     */
    def compress(input: String) = {
        // 2011-03-13 20:48:01 - dmilith - TODO: implement check for too short string to compress (<40 chars)
        val byteInput = input.getBytes
        val bos = new ByteArrayOutputStream(byteInput.length)
        val buf = new Array[Byte](128)
        val compressor = new Deflater
        compressor.setLevel(Deflater.BEST_COMPRESSION)
        compressor.setInput(byteInput)
        compressor.finish
        while (!compressor.finished) {
            val count = compressor.deflate(buf)
            bos.write(buf, 0, count)
        }
        try {
            bos.close
        } catch {
            case e: Exception => {
                e.printStackTrace // 2011-03-13 17:38:52 - dmilith - XXX: temporary code
            }
        }
        val compressedByte = bos.toByteArray
        val compressedString = new String(compressedByte)
        log.debug("Original string length: %d, Compressed one: %d".format(input.length, compressedString.length))
        compressedString
    }


    /**
     *   simple String decompression (zip inflate/deflate)
     *
     *  @author dmilith
     */
    def decompress(input: String) = {
        // Decompress the data
        val decompressor = new Inflater
        val buf = new Array[Byte](128)
        val compressedByte = input.getBytes
        decompressor.setInput(compressedByte)
        val bos = new ByteArrayOutputStream(compressedByte.length)

        while (!decompressor.finished) {
            try {
                val count = decompressor.inflate(buf)
                bos.write(buf, 0, count)
            } catch {
                case e: DataFormatException =>
                    log.error("Data Format Exception: %s", e)
            }
        }
        try {
            bos.close
        } catch {
            case e: Exception =>
                log.error("Exception in decompress: " + e)
        }
        val decompressedByte = bos.toByteArray
        val decompressedString = new String(decompressedByte)
        log.debug("Compressed string length: %d, Decompressed one: %d".format(input.length, decompressedString.length))
        decompressedString
    }


    /**
     *   converts seconds to friendly format: hh-mm-ss
     *
     *  @author dmilith
     */
    def secondsToHMS(seconds: Int) = {
        val calendar = new GregorianCalendar(0,0,0,0,0,0)
        calendar.set(Calendar.SECOND, seconds)
        "%02dh:%02dm:%02ds".format(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND))
    }


    /**
     *  Get all live threads of ServeD. Useful only when debugging.
     *
     *  @author dmilith
     */
    def getAllLiveThreads = log.trace("Live threads list:\n%s".format(
        Thread.getAllStackTraces.map{
            th =>
                "%s - %s\n".format(
                    th._1,
                    th._2.toList.map{
                        elem =>
                            "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(
                                elem.getFileName,
                                elem.getClassName,
                                elem.getMethodName,
                                elem.getLineNumber,
                                elem.isNativeMethod)
                    }
                )
        }))


    /**
     *  Returns size (in bytes) of given object in JVM memory
     *
     *  @example println(sizeof(new Date))
     *  @author dmilith
     */
    def sizeof(any: Any) = ObjectProfiler.sizeof(any)


    /**
     *  Adds a hook for SIGINT/ SIGTERM signals to gracefully close the app
     *
     *  @example addShutdownHook { closeMyDatabase; info("Dying!") }
     *  @author dmilith
     */
    def addShutdownHook(block: => Unit) =
        Runtime.getRuntime.addShutdownHook(
            new Thread {
                override def run = block
            }
        )

    def handleSignal(name: String)(block: => Unit) =
        Signal.handle(new Signal(name), new SignalHandler {
            def handle(sig: Signal) {
                log.warn("Signal called: " + name)
                block
            }
        })




    /**
     *   Returns uid of current logged in user
     *
     *  @author dmilith
     */
    def getUserUid = {
        clib.getuid
    }


    /**
     *   counts time spent on operation in given block
     *
     *  @author dmilith
     */
    def bench(block: => Unit) = {
        val start = System.currentTimeMillis
        block
        System.currentTimeMillis - start
    }


    /**
     * Create new SvdLoopThread with provided function
     *
     * @author teamon
     */
    def loopThread(f: => Unit) = new SvdLoopThread(f)


    /**
     * Checks if file exists
     *
     * @author teamon
     */
    def fileExists(path: String) = (new File(path)).exists


    /**
     * Removes any file or directory
     *
     * @author teamon
     * @author dmilith
     */
    def rm_r(path: String) = try { FileUtils.forceDelete(path) } catch { case x: Throwable => log.warn(x.getMessage) }


    /**
     *   Find file in given directory. Named params available: name, root, extensions and recursive
     *
     *  @author dmilith
     */
    def findFileInDir(
        name: String,
        root: String = System.getProperty("user.dir"),
        extensions: Array[String] = Array("scala", "java"),
        recursive: Boolean = true): String = {
            val files = FileUtils.listFiles(root, extensions, recursive)
            val filter = new FilenameFilter {
                def accept(dir: File, aName: String) =
                    aName.lastIndexOf(name) != -1
            }
            val iterator = files.iterator
            log.trace("Looking for: %s in %s. Extensions: %s, Recursive: %s".format(
                name,
                root,
                extensions.mkString(" "),
                recursive))

            while (iterator.hasNext) {
                val file = iterator.next
                file match {
                    case f: File =>
                        if (filter.accept(root, f.toString))
                            return f.getAbsolutePath

                }
            }

        ""
    }


    /**
     *   Recursive list files in given path
     *
     *  @author dmilith, teamon
     */
    def recursiveListFilesFromPath(file: File): List[File] = {
        file :: (
            if (file.isDirectory)
                Option(file.listFiles).map(
                    e =>
                        e.par.toList.flatMap(recursiveListFilesFromPath)
                ) getOrElse Nil
            else Nil)
    }


    /**
     *   List of directories from given location
     *
     *  @author dmilith
     */
    def listDirectories(location: String) =
        (new File(location)).listFiles.filter(_.isDirectory)


    /**
     *   List of files from given location
     *
     *  @author dmilith
     */
    def listFiles(location: String) =
        (new File(location)).listFiles.filterNot(_.isDirectory)


    /**
    *   Checks if a specific port is available for user
    *
    * @author dmilith
    */
    def portAvailable(port: Int): Boolean = {
        try {
            val ss = new ServerSocket(port)
            ss.setReuseAddress(true)
            val ds = new DatagramSocket(port)
            ds.setReuseAddress(true)
            if (ds != null)
                ds.close
            if (ss != null)
                ss.close
            return true
        } catch {
            case e: IOException =>
                println("ERROR: IOException: %s".format(e))
        }
        false
    }


    /**
     *  Get list of fields of case class using reflection
     *
     *  @author dmilith
     */
    def caseClassFields(cc: AnyRef) =
        (Map[String, String]() /: cc.getClass.getDeclaredFields) {
            (a, f) =>
                f.setAccessible(true)
                a + (f.getName -> f.get(cc).toString)
        }


    // XXX: TODO: FIXME: this javaish implementation sucks. We'll need good efficient solution, probably C lib
    def fileToString(file: File, encoding: String = "UTF-8") = {
        val inStream = new FileInputStream(file)
        val outStream = new ByteArrayOutputStream
        try {
            var reading = true
            while (reading) {
                inStream.read match {
                    case -1 =>
                        reading = false
                    case c =>
                        outStream.write(c)
                    }
            }
            outStream.flush
        } finally {
            inStream.close
        }
        new String(outStream.toByteArray, encoding)
    }


    def loadList(file: String) = {
        try {
            Some(
                fileToString(file).split(" ").toList.filterNot(_.isEmpty)
            ).getOrElse(Nil)
        } catch {
            case e: FileNotFoundException =>
                log.debug("No file found: %s".format(file))
                Nil
        }
    }


    def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B =
        try { f(param) } finally { param.close() }


    def writeToFile(fileName: String, data: String) =
        using (new FileWriter(fileName)) {
            fileWriter => fileWriter.write(data)
    }

    def fssecure(path: String)(f: String => Unit) {
        f(path)
    }

    def touch(path: String) = fssecure(path) {
        p => FileUtils touch p
    }



    /**
     *  Returns true if given ip matches with any of server IPs.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def isIPBoundToCurrentServer(ip: String): Boolean = {
        log.trace("Checking ip: %s", ip)
        val ifcs = NetworkInterface.getNetworkInterfaces
        while (ifcs.hasMoreElements) {
            val ifc = ifcs.nextElement
            log.debug("IPs of interface: %s", ifc.getDisplayName)
            val adresses = ifc.getInetAddresses
            while (adresses.hasMoreElements) {
                val element = adresses.nextElement.getHostAddress
                log.trace("IP address: %s vs %s", element, ip)
                if (element == ip)
                    return true
            }
        }
        false
    }


    /**
     *  Code to validate given domain.
     *  It will result:
     *      None => when no problems occured. Everything's fine, domain is bindable under one of current server IPs.
     *      Some => with some problem
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def validateDomain(domain: String) = {
        try {
            domain match {
                case "" =>
                    Some("Domain cannot be empty!")

                case _ =>
                    if (domain.matches(SvdConfig.matcherFQDN)) { // domain must be fully qualified
                        val domainAddress = InetAddress.getByName(domain).getHostAddress
                        if (isIPBoundToCurrentServer(domainAddress)) // domain must be bound to this server
                            None //NOTE: No errors. Domain's fine.
                        else
                            Some("Domain is incorrectly bound or DNS not yet propagated.")
                    } else {
                        Some("Domain must be fully qualified, registered domain name.")
                    }
            }

        } catch {
            case e: UnknownHostException =>
                Some("Unknown host: %s!".format(domain))

            case e: java.lang.AbstractMethodError =>
                Some("Domain is bound to unknown or wrong server.")

            case e: Throwable =>
                Some("Exception %s for: %s!".format(e, domain))

        }
    }

}

