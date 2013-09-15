/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served

package object db {
    type Persistent = com.verknowsys.served.api.Persistent
    type UUID = java.util.UUID
    def randomUUID = java.util.UUID.randomUUID
}
