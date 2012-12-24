package com.verknowsys.served.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CSystemTime extends Library {
    public static final CSystemTime instance = (CSystemTime) Native.loadLibrary("systemtime", CSystemTime.class);

    boolean adjustSystemTime(double offset);

}

