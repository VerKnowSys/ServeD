package com.verknowsys.served.utils

import com.verknowsys.served.testing._
import sun.security.rsa.RSAPublicKeyImpl

class KeyUtilsTest extends DefaultTest {
    "KeyUtils" should "parse valid public key" in {
        val data = testPublicKey
        
        val key = KeyUtils.load(data)
        key should be ('defined)
        key.get.getClass should equal (classOf[RSAPublicKeyImpl])
    }
    
    it should "return None for invalid key in" in {
        val key = KeyUtils.load("some weird key")
        key should equal (None)
    }
}
