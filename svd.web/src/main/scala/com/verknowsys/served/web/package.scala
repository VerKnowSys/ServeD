/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.web


import org.json4s._
import org.json4s.native.JsonMethods._


package object webImplicits {


    implicit def convertUUIDtoString(u : java.util.UUID) = u.toString()


    implicit def convertAnythingToJValueIfNecessary(a: JValue) = compact(render(a))


}
