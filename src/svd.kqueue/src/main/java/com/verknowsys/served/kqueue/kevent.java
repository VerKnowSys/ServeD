package com.verknowsys.served.kqueue;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class kevent extends Structure {
	/// identifier for this event
	public NativeLong ident;
	/// filter for event
	public short filter;
	/// general flags
	public short flags;
	/// filter-specific flags
	public int fflags;
	/// filter-specific data
	public NativeLong data;
	/**
	 * opaque user data identifier<br>
	 * C type : void*
	 */
	public Pointer udata;
	public kevent() {
		super();
	}
	/**
	 * @param ident identifier for this event<br>
	 * @param filter filter for event<br>
	 * @param flags general flags<br>
	 * @param fflags filter-specific flags<br>
	 * @param data filter-specific data<br>
	 * @param udata opaque user data identifier<br>
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
	public static class ByReference extends kevent implements Structure.ByReference {
		
	};
	public static class ByValue extends kevent implements Structure.ByValue {
		
	};
}
