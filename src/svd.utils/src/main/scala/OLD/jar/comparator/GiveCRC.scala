// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.jar.comparator


/**
 * User: dmilith
 * Date: Jun 27, 2009
 * Time: 8:23:38 PM
 */

object GiveCRC {
    def main(args: Array[String]) {
        if (args.length == 0) exit
        val comparator = new JarEntryComparator
        comparator.loadAndThrowListOfCrcs(args(0)).map {a => print(a + ",")}
    }

}
