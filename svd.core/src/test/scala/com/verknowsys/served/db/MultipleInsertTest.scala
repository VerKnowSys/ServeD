package com.verknowsys.served.db

import org.specs._
import com.verknowsys.served.systemmanager.SvdAccountUtils

class MultipleInsertTest extends DatabaseTest {
    "MultipleInsertTest" should {
        doBefore { connect }
        doAfter { disconnect }
        
        "store Users" in {
            val svdAccountUtils = new SvdAccountUtils(db)
            import svdAccountUtils._
            
            (1 to 10) foreach { i =>
                val ruid = randomUserUid
                if (!userUIDRegistered(ruid)) {
                    registerUserAccount(ruid, "żółć")
                }
            }
        }
    }
}
