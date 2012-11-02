package com.verknowsys.served.api


// /**
//  *  @author dmilith
//  *
//  *   Defines software requirement to be downloaded from binary repository for actual OS, set by SvdConfig.binarySoftwareRepository
//  */
// case class SvdSoftwareRequirement(
//         name: String
//     )


/**
 *  @author dmilith
 *
 *   Defines config of hooks passed to SvdService
 */
case class SvdServiceConfig(
        name: String,
        install: SvdShellOperations,
        configure: SvdShellOperations,
        start: SvdShellOperations,
        afterStart: SvdShellOperations,
        stop: SvdShellOperations,
        afterStop: SvdShellOperations,
        reload: SvdShellOperations,
        validate: SvdShellOperations
    )


/**
 *  @author dmilith
 *
 *   Defines shell operation required by SvdShell
 */
case class SvdShellOperations(
        commands: List[String],
        expectStdOut: List[String] = Nil,
        expectStdErr: List[String] = Nil,
        waitForOutputFor: Int = 5
    )
