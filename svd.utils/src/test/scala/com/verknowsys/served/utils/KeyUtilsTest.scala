package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import sun.security.rsa.RSAPublicKeyImpl

class KeyUtilsTest extends DefaultTest {
    "KeyUtils" should "parse public key" in {
        val data = testPublicKey
        
        val key = KeyUtils.load(data)
        key should be ('defined)
        key.get.getClass should equal (classOf[RSAPublicKeyImpl])
    }
}
