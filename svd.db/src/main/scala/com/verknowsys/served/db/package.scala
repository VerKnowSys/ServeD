package com.verknowsys.served

package object db {
    implicit def dbclient2db(client: DBClient): DB = client.current
}
