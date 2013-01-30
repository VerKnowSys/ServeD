/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.cli

import com.verknowsys.served.api.RemoteSession


/**
 * CLI
 *
 * @author teamon
 */
class ApiClient(svd: Svd) {
    implicit val served = svd

    val commands = (
        LoggerCommands ::
        GitCommands ::
        GeneralCommands ::
        Nil
    ).map(_.commands).reduce(_ orElse _)

    // Connect to ServeD
    // request(General.Connect(userUid)) {
    //     case ApiSuccess =>
    //         info("ServeD interactive shell. Welcome %s", userUid)
    //         prompt(new Prompt)
    //     case ApiError(message) =>
    //         error(message)
    //         quit
    // }



    /**
     * Show prompt and read arguments
     *
     * @author teamon
     */
    def prompt(in: Prompt) {
        val args = in.readLine
        if(!args.isEmpty) commands(args.toList)
        prompt(in)
    }

    /**
     * Get system username
     *
     * @author teamon
     */
    private lazy val userUid = 501 // 2011-07-07 23:20:40 - dmilith - XXX: yet another place requiring code which is available in utils (getuid)
}

object Runner {
    def main(args: Array[String]) {
        if(args.length == 2) {
            RemoteSession(args(0), args(1).toInt) match {
                case Some(svd) => new ApiClient(svd)
                case None => error("Unable to connect to ServeD")
            }
        } else {
            error("Usage: com.verknowsys.served.cli.Runner HOST PORT")
        }
    }
}
