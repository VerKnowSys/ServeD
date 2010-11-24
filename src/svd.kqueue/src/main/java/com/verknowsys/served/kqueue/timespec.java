package com.verknowsys.served.kqueue;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public class timespec extends Structure {
	public NativeLong tv_sec;
	public NativeLong tv_nsec;
	public timespec() {
		super();
	}
	public timespec(NativeLong tv_sec, NativeLong tv_nsec) {
		super();
		this.tv_sec = tv_sec;
		this.tv_nsec = tv_nsec;
	}
	public static class ByReference extends timespec implements Structure.ByReference {
		
	};
	public static class ByValue extends timespec implements Structure.ByValue {
		
	};
}
