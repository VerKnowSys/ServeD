package com.verknowsys.served.systemmanager.limits


// 2011-01-25 20:41:51 - dmilith - TODO: implement checking connected users/ sockets to given process

/**
 *   Describes Ressident Set Size of system memory, used by process
 */
class RSS(val max: Int, val timeout: Int = -1)


/**
 *   Controls cpu time used by process
 */
class CPU(val max: Int, val timeout: Int = -1)


/**
 *   Controls amount of used Input/Output 
 */
class IO(val max: Int, val timeout: Int = -1)


class IRQ(val max: Int, val timeout: Int = 1)

/**
 *   Controls amount of used space in 'timeout' period of time.
 */
class SPACE(val max: Int, val timeout: Int = -1)


/**
 *   Controls amount of received bytes through network device
 */
class RX(val max: Int, val timeout: Int = -1) // 2011-01-26 00:16:38 - dmilith - NOTE: rather period of time


/**
 *   Controls amount of sent bytes through network device
 */
class TX(val max: Int, val timeout: Int = -1)