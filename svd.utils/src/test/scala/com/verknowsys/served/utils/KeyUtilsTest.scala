package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import scala.io.Source
import sun.security.rsa.RSAPublicKeyImpl

class KeyUtilsTest extends DefaultTest {
    "KeyUtils" should "parse public key" in {
        val file = Source.fromURL(getClass.getResource("/test_key_rsa.pub"))
        val data = file.getLines.mkString("\n")
        
        val key = KeyUtils.load(data)
        key should be ('defined)
        key.get.getClass should equal (classOf[RSAPublicKeyImpl])
    }
}
