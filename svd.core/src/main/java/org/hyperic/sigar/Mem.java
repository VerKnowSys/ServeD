/*****************************************************
 * WARNING: this file was generated by -e
 * on Thu Jun  9 04:13:13 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * Mem sigar class.
 */
public class Mem implements java.io.Serializable {

    private static final long serialVersionUID = 10181L;

    public Mem() { }

    public native void gather(Sigar sigar) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getMem() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getMem
     */
    static Mem fetch(Sigar sigar) throws SigarException {
        Mem mem = new Mem();
        mem.gather(sigar);
        return mem;
    }

    long total = 0;

    /**
     * Get the Total system memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>lsattr -El sys0 -a realmem</code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total system memory
     */
    public long getTotal() { return total; }
    long ram = 0;

    /**
     * Get the System Random Access Memory (in MB).<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>lsattr -El sys0 -a realmem</code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>cat /proc/mtrr | head -1</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return System Random Access Memory (in MB)
     */
    public long getRam() { return ram; }
    long used = 0;

    /**
     * Get the Total used system memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total used system memory
     */
    public long getUsed() { return used; }
    long free = 0;

    /**
     * Get the Total free system memory (e.g. Linux plus cached).<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total free system memory (e.g. Linux plus cached)
     */
    public long getFree() { return free; }
    long actualUsed = 0;

    /**
     * Get the Actual total used system memory (e.g. Linux minus buffers).<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Actual total used system memory (e.g. Linux minus buffers)
     */
    public long getActualUsed() { return actualUsed; }
    long actualFree = 0;

    /**
     * Get the Actual total free system memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Actual total free system memory
     */
    public long getActualFree() { return actualFree; }
    double usedPercent = 0;

    /**
     * Get the Percent total used system memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Percent total used system memory
     */
    public double getUsedPercent() { return usedPercent; }
    double freePercent = 0;

    /**
     * Get the Percent total free system memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>free</code><br>
     * <li> Solaris: <code></code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Percent total free system memory
     */
    public double getFreePercent() { return freePercent; }

    void copyTo(Mem copy) {
        copy.total = this.total;
        copy.ram = this.ram;
        copy.used = this.used;
        copy.free = this.free;
        copy.actualUsed = this.actualUsed;
        copy.actualFree = this.actualFree;
        copy.usedPercent = this.usedPercent;
        copy.freePercent = this.freePercent;
    }
    public String toString() {
        return
            "Mem: " +
            (this.total / 1024) + "K av, " +
            (this.used / 1024) + "K used, " +
            (this.free / 1024) + "K free";
    }

    public Map toMap() {
        Map map = new HashMap();
        String strtotal = 
            String.valueOf(this.total);
        if (!"-1".equals(strtotal))
            map.put("Total", strtotal);
        String strram = 
            String.valueOf(this.ram);
        if (!"-1".equals(strram))
            map.put("Ram", strram);
        String strused = 
            String.valueOf(this.used);
        if (!"-1".equals(strused))
            map.put("Used", strused);
        String strfree = 
            String.valueOf(this.free);
        if (!"-1".equals(strfree))
            map.put("Free", strfree);
        String stractualUsed = 
            String.valueOf(this.actualUsed);
        if (!"-1".equals(stractualUsed))
            map.put("ActualUsed", stractualUsed);
        String stractualFree = 
            String.valueOf(this.actualFree);
        if (!"-1".equals(stractualFree))
            map.put("ActualFree", stractualFree);
        String strusedPercent = 
            String.valueOf(this.usedPercent);
        if (!"-1".equals(strusedPercent))
            map.put("UsedPercent", strusedPercent);
        String strfreePercent = 
            String.valueOf(this.freePercent);
        if (!"-1".equals(strfreePercent))
            map.put("FreePercent", strfreePercent);
        return map;
    }

}
