package com.verknowsys.served.systemmanager.storage;

import java.util.Date;
import java.sql.Timestamp;

public class ProcessInfo {
    public int pid;
    public String name;
    public int cpu;
    public int mem;
    public Timestamp time;

    public ProcessInfo(int pid, String name, int cpu, int mem, Timestamp time) {
        this.pid = pid;
        this.name = name;
        this.mem = mem;
        this.time = time;
        this.cpu = cpu;
    }

    public ProcessInfo(int pid, String name, int cpu, int mem) {
        this(pid, name, cpu, mem, new Timestamp(new Date().getTime()));
    }
    
    public String toString(){
        return "ProcessInfo(" + pid + "," + name + "," + cpu + "," + mem + "," + time + ")";
    }
}
