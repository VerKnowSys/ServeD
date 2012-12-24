package com.verknowsys.served.systemmanager.storage;

import java.sql.Timestamp;
import java.util.Date;

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
    
    public boolean equals(Object obj){
        if(obj instanceof ProcessInfo){
            ProcessInfo that = (ProcessInfo)obj;
            return (
                that.pid == this.pid &&
                that.cpu == this.cpu &&
                that.mem == this.mem &&
                that.name.equals(this.name)
            );
        } else {
            return false;
        }
    }
}
