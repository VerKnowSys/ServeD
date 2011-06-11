package com.verknowsys.served.utils;

import com.sun.jna.*;

/**
 * clib wrapper for spawning external processes
 *
 * @author dmilith
 */
public interface SvdWrapLibrary extends Library {
    
    public static final SvdWrapLibrary instance = (SvdWrapLibrary) Native.loadLibrary("svdwrap", SvdWrapLibrary.class);

    public String spawn(int user_uid, String command, String output_file);
    
}
