package com.verknowsys.served.utils


import com.verknowsys.served._

import org.apache.commons.io.FileUtils
import clime.messadmin.providers.sizeof.ObjectProfiler
import scala.collection.JavaConversions._
import scala.util.matching.Regex
import akka.util.Logging
import scala.io._
import com.sun.jna.Native
import java.io._
import java.util.UUID
import java.util.{Calendar, GregorianCalendar}
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater
import java.util.ArrayList
import java.util.regex.Pattern


/**
 * SvdUtils object containing common functions
 * 
 * @author dmilith
 *   
 */
object SvdUtils extends Logging {
    
    
    /**
    *   @author dmilith
    *   
    *   Get and spread props for all believers..
    *   
    */   
    lazy val props = SvdConfig.props
    
    
    /**
    *   @author dmilith
    *   
    *   check and inform when current user isn't superuser (root)
    *   
    */   
    def rootCheck {
        System.getProperty("user.name") match {
            case "root" =>
            case _ =>
                lazy val err = "%s must be run as root user to perform some operations!".format(this.getClass)
                error(err)
        }
    }
    

    /**
     *  @author dmilith
     *
     *   returns true if running system matches BSD
     */
    def isBSD = System.getProperty("os.name").contains("BSD")
    
    
    /**
     *  @author dmilith
     *
     *   returns true if running system matches Linux
     */
    def isLinux = System.getProperty("os.name").contains("Linux")
    
    
    /**
     *  @author dmilith
     *
     *   Generate unique identifier
     */
    def newUuid = UUID.randomUUID
    
    
    /**
     *  @author dmilith
     *
     *  Checks and creates (if missing) given directory name
     *
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
     *  @author dmilith
     *
     *   simple String compression (zip inflate/deflate)
     */
    def compress(input: String) = {
        val byteInput = input.getBytes
        val bos = new ByteArrayOutputStream(byteInput.length)
        val buf = new Array[Byte](1024)
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
        log.debug("Original string:\n%s\n\nCompressed one:\n%s".format(input, compressedString))
        compressedString
    }
    
    
    def decompress(input: String) = {
        // Decompress the data
        val decompressor = new Inflater
        val buf = new Array[Byte](1024)
        val compressedByte = input.getBytes
        decompressor.setInput(compressedByte)
        val bos = new ByteArrayOutputStream(compressedByte.length)
        
        while (!decompressor.finished) {
            try {
                val count = decompressor.inflate(buf)
                bos.write(buf, 0, count)
            } catch {
                case e: DataFormatException =>
            }
        }
        try {
            bos.close
        } catch {
            case e: Exception =>
                log.trace("Exception in decompress: " + e)
        }

        val decompressedByte = bos.toByteArray
        val decompressedString = new String(decompressedByte)
        log.debug("Compressed string:\n%s\n\nDecompressed one:\n%s".format(input, decompressedString))
    }
    

    /**
     *  @author dmilith
     *
     *   converts seconds to friendly format: hh-mm-ss
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
     *  @author dmilith
     *
     *  Checks and creates (if missing) ServeD vendor dir
     *
     */
    def checkOrCreateVendorDir = checkOrCreateDir(SvdConfig.homePath + SvdConfig.vendorDir)

    
    /**
     *  @author dmilith
     *
     *  Get all live threads of ServeD. Useful only when debugging.
     *
     */
    def getAllLiveThreads = log.trace("Live threads list:\n%s".format(Thread.getAllStackTraces.toList.map{ th => "%s - %s\n".format(th._1, th._2.toList.map{ elem => "File name: %s, Class name: %s, Method name: %s, Line number: %d, (is Native? %b)\n".format(elem.getFileName, elem.getClassName, elem.getMethodName, elem.getLineNumber, elem.isNativeMethod)})}))
        

    /**
     *  @author dmilith
     *
     *  Returns size (in bytes) of given object in JVM memory
     *
     *  @example
     *  println(sizeof(new Date))
     *
     */
    def sizeof(any: Any) = ObjectProfiler.sizeof(any)
    
    
    /**
     *  @author dmilith
     *
     *  Adds a hook for SIGINT/ SIGTERM signals to gracefully close the app
     *
     *  @example
     *  addShutdownHook {
     *     closeMySockets
     *     info("Dying!")
     *  }
     *
     */
    def addShutdownHook(block: => Unit) =
        Runtime.getRuntime.addShutdownHook(
            new Thread {
                override def run = block
            }
        )


    /**
     *  @author dmilith
     *
     *   counts time spent on operation in given block
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
    def fileExists(path: String) = (new java.io.File(path)).exists
    
    
    /** 
     * Removes directory
     * 
     * @author teamon
     */
    def rmdir(path: String) = try { FileUtils.forceDelete(path) } catch { case x: Throwable => log.warn(x.getMessage) }


    /**
     *  @author dmilith
     *
     *   Find file in given directory. Named params available: name, root, extensions and recursive
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
            log.trace("Looking for: %s in %s. Extensions: %s, Recursive: %s".format(name, root, extensions.mkString(" "), recursive))
            while (iterator.hasNext) {
                val file = iterator.next.asInstanceOf[File] // 2011-02-01 06:44:08 - dmilith - XXX: try matcher here
                if (filter.accept(root, file.toString)) {
        			return file.getAbsolutePath
        		}
            }
        ""
    }


    /**
     *  @author dmilith
     *
     *   Recursive list files in given path
     */
    def recursiveListFilesFromPath(filePath: File): Array[File] = {
        val out = filePath.listFiles
        if (out != null)
            out ++ out.filter(_.isDirectory).flatMap(recursiveListFilesFromPath(_))
        else
            Array()
    }

    
    /**
     *  @author dmilith
     *
     *   Recursive list files in given path + match by Regex
     */
    def recursiveListFilesByRegex(filePath: File, regex: Regex): Array[File] = {
        val list = filePath.listFiles
        val out = list.filter(filePath => regex.findFirstIn(filePath.getName).isDefined)
        if (out != null)
            out ++ list.filter(_.isDirectory).flatMap(recursiveListFilesByRegex(_, regex))
        else
            Array()
    }
    
    
    /** 
     * Changes owner of file at given path
     * 
     * @author dmilith
     */
    def chown(path: String, user: Int, group: Int = SvdConfig.defaultUserGroup, recursive: Boolean = true) =
        if (!(new File(path)).exists) {
            log.warn("Chown: File/ path doesn't exists! Cannot chown non existant file/ directory! IGNORING!")
            false
        } else {
            import CLibrary._
            val clib = CLibrary.instance
            val files = if (recursive) SvdUtils.recursiveListFilesFromPath(new File(path)).toList else (new File(path) :: Nil)
            log.trace("chown(path: %s, user: %d, group: %d, recursion: %s): File list: %s. Amount of files: %s".format(path, user, group, recursive, files.mkString(", "), files.length))

            for (file <- files) {
                log.trace("chowning: %s".format(file.getAbsolutePath))
                if (clib.chown(file.getAbsolutePath, user, group) != 0)
                    throw new Exception("Error occured while chowning: %s".format(file))
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
            import CLibrary._
            val clib = CLibrary.instance
            val files = if (recursive) SvdUtils.recursiveListFilesFromPath(new File(path)).toList else (new File(path) :: Nil)
            log.trace("chmod(path: %s, mode: %d, recursion: %s)".format(path, mode, recursive))

            for (file <- files) {
                log.trace("chmoding: %s".format(file.getAbsolutePath))
                if (clib.chmod(file.getAbsolutePath, mode) != 0)
                    throw new Exception("Error occured while chmoding: %s".format(file))
            }
            true            
        }
    
    
}

