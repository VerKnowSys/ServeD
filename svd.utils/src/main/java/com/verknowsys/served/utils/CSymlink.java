package com.verknowsys.served.utils;

import com.sun.jna.*;

public interface CSymlink extends Library {
    public static final CSymlink instance = (CSymlink) Native.loadLibrary("symlink", CSymlink.class);

    boolean isSymlink(String path);

}
