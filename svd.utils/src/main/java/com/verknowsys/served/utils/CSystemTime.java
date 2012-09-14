package com.verknowsys.served.utils;

import com.sun.jna.*;

public interface CSystemTime extends Library {
    public static final CSystemTime instance = (CSystemTime) Native.loadLibrary("systemtime", CSystemTime.class);

    Boolean adjustSystemTime(double offset);

}

