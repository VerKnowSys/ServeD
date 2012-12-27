/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served


import org.json4s._
import org.json4s.ParserUtil._
import org.json4s.native.JsonMethods._
import scala.io.Source
import java.io.FileNotFoundException

import com.verknowsys.served._
import com.verknowsys.served.utils._
import com.verknowsys.served.api._




/**
 *  Manages Security of Account
 *
 * @author Daniel (dmilith) Dettlaff
 */
class SvdAccountSecurityCheck(account: SvdAccount) extends SvdUtils {

    implicit def uuidToString(u: UUID) = u.toString
    implicit val formats = DefaultFormats // Brings in default date formats etc.


    /**
     *  Generate uid agnostic account security key.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def securityKey = sha1(account.uuid + SvdConfig.defaultSecurityBaseKey)


    /**
     *  Load key from file matching uid of account
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def loadSystemKeyOfAccount = {
        val systemAuthAccountDir = SvdConfig.systemHomeDir / "Security"
        checkOrCreateDir(systemAuthAccountDir)

        val systemAuthFileOfAccount = systemAuthAccountDir / securityKey + SvdConfig.defaultSoftwareTemplateExt
        try {
            // NOTE: just read file. Don't even try to remove user authkey file!
            val auuid = sha1(account.uuid)
            val json = parse(Source.fromFile(systemAuthFileOfAccount).getLines.mkString)
            val userKey = (json \ "usha").extract[String]
            log.debug("File found for uuid: %s, loaded content: %s, securityKey: %s", auuid, userKey, securityKey)
            log.trace("SHA: %s VS: %s".format(auuid, userKey))
            log.debug("Content UUID matches. Auth successfull.")
            if (userKey == auuid)
                Some(compact(render(json)))
            else
                None

        } catch {
            case e: FileNotFoundException =>
                log.warn("No account file found: %s", systemAuthFileOfAccount)
                None

            case e: ParseException =>
                log.error("File format exception: %s", e)
                None

            case e: Exception =>
                log.error("Exception: %s", e)
                None
        }
    }


    /**
     *  Load Option[String] with JSON content.
     *
     * @author Daniel (dmilith) Dettlaff
     */
    def load = loadSystemKeyOfAccount


}
