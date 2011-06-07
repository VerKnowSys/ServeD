/*****************************************************
 * WARNING: this file was generated by -e
 * on Sat Mar  5 03:11:36 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * ProcCpu sigar class.
 */
public class ProcCpu implements java.io.Serializable {

    private static final long serialVersionUID = 6748L;

    public ProcCpu() { }

    public native void gather(Sigar sigar, long pid) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getProcCpu() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getProcCpu
     */
    static ProcCpu fetch(Sigar sigar, long pid) throws SigarException {
        ProcCpu procCpu = new ProcCpu();
        procCpu.gather(sigar, pid);
        return procCpu;
    }

    double percent = 0;

    /**
     * Get the Process cpu usage.<p>
     * Supported Platforms: All.
     * <p>
     * @return Process cpu usage
     */
    public double getPercent() { return percent; }
    long lastTime = 0;

    /**
     * Get the last_time.<p>
     * Supported Platforms: All.
     * <p>
     * @return last_time
     */
    public long getLastTime() { return lastTime; }
    long startTime = 0;

    /**
     * Get the Time process was started in seconds.<p>
     * Supported Platforms: All.
     * <p>
     * @return Time process was started in seconds
     */
    public long getStartTime() { return startTime; }
    long user = 0;

    /**
     * Get the Process cpu user time.<p>
     * Supported Platforms: All.
     * <p>
     * @return Process cpu user time
     */
    public long getUser() { return user; }
    long sys = 0;

    /**
     * Get the Process cpu kernel time.<p>
     * Supported Platforms: All.
     * <p>
     * @return Process cpu kernel time
     */
    public long getSys() { return sys; }
    long total = 0;

    /**
     * Get the Process cpu time (sum of User and Sys).<p>
     * Supported Platforms: All.
     * <p>
     * @return Process cpu time (sum of User and Sys)
     */
    public long getTotal() { return total; }

    void copyTo(ProcCpu copy) {
        copy.percent = this.percent;
        copy.lastTime = this.lastTime;
        copy.startTime = this.startTime;
        copy.user = this.user;
        copy.sys = this.sys;
        copy.total = this.total;
    }

    public Map toMap() {
        Map map = new HashMap();
        String strpercent = 
            String.valueOf(this.percent);
        if (!"-1".equals(strpercent))
            map.put("Percent", strpercent);
        String strlastTime = 
            String.valueOf(this.lastTime);
        if (!"-1".equals(strlastTime))
            map.put("LastTime", strlastTime);
        String strstartTime = 
            String.valueOf(this.startTime);
        if (!"-1".equals(strstartTime))
            map.put("StartTime", strstartTime);
        String struser = 
            String.valueOf(this.user);
        if (!"-1".equals(struser))
            map.put("User", struser);
        String strsys = 
            String.valueOf(this.sys);
        if (!"-1".equals(strsys))
            map.put("Sys", strsys);
        String strtotal = 
            String.valueOf(this.total);
        if (!"-1".equals(strtotal))
            map.put("Total", strtotal);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}