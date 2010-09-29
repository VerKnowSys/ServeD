// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.maintainer


import com.verknowsys.served._
import com.verknowsys.served.maintainer._

import scala.io.Source
import scala._
import org.specs._


class SvdMaintainerTest extends SpecificationWithJUnit {
    "Maintainer" should {
        "Be efficient with counting folder size" in {
            val start = (new java.util.Date).getTime
            val size: Long = SvdAccountManager.getAccountSize("_carddav") getOrElse 0
            val stop = (new java.util.Date).getTime
            System.out.println("Count size result time: " + (stop - start) + "ms. Returned size: " + (size / Config.sizeMultiplier) + " KiB")
        }
    }

}
