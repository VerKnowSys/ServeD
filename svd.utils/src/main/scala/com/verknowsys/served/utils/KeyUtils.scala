package com.verknowsys.served.utils

object KeyUtils extends Logging {
    def load(key: String) = catchException {
        PublicKeyReaderUtil.load(key)
    }
}
