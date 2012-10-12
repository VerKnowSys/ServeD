package com.verknowsys.served.web


import net.liftweb.json._


package object webImplicits {


    implicit def convertUUIDtoString(u : java.util.UUID) = u.toString()


    implicit def convertAnythingToJValueIfNecessary(a: JValue) = compact(render(a))


}