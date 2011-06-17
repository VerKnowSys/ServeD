package com.verknowsys.served

package object db {
    type Persistent = com.verknowsys.served.api.Persistent
    type UUID = java.util.UUID
    def randomUUID = java.util.UUID.randomUUID
}
