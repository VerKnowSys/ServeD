/*
 * © Copyright 2008-2013 Daniel (dmilith) Dettlaff. ® All Rights Reserved.
 * This Software is a close code project. You may not redistribute this code without permission of author.
 */

package com.verknowsys.served.utils;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * C struct kevent * mapping
 *
 * @author teamon 
 */
public class kevent extends Structure {
    public NativeLong ident; // identifier for this event
    public short filter;    // filter for event
    public short flags;     // general flags
    public int fflags;      // filter-specific flags
    public NativeLong data; // filter-specific data

    /**
     * opaque user data identifier
     * C type : void*
     */
    public Pointer udata;

    public kevent() {
        super();
    }

    /**
     * @param ident identifier for this event
     * @param filter filter for event
     * @param flags general flags
     * @param fflags filter-specific flags
     * @param data filter-specific data
     * @param udata opaque user data identifier
     * C type : void*
     */
    public kevent(NativeLong ident, short filter, short flags, int fflags, NativeLong data, Pointer udata) {
        super();
        this.ident = ident;
        this.filter = filter;
        this.flags = flags;
        this.fflags = fflags;
        this.data = data;
        this.udata = udata;
    }

    // public static class ByReference extends kevent implements Structure.ByReference {};
    // public static class ByValue extends kevent implements Structure.ByValue {};
}
