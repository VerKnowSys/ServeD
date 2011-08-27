/*****************************************************
 * WARNING: this file was generated by -e
 * on Thu Jun  9 04:13:13 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * ProcMem sigar class.
 */
public class ProcMem implements java.io.Serializable {

    private static final long serialVersionUID = 7985L;

    public ProcMem() { }

    public native void gather(Sigar sigar, long pid) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getProcMem() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getProcMem
     */
    static ProcMem fetch(Sigar sigar, long pid) throws SigarException {
        ProcMem procMem = new ProcMem();
        procMem.gather(sigar, pid);
        return procMem;
    }

    long size = 0;

    /**
     * Get the Total process virtual memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total process virtual memory
     */
    public long getSize() { return size; }
    long resident = 0;

    /**
     * Get the Total process resident memory.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total process resident memory
     */
    public long getResident() { return resident; }
    long share = 0;

    /**
     * Get the Total process shared memory.<p>
     * Supported Platforms: AIX, HPUX, Linux, Solaris.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total process shared memory
     */
    public long getShare() { return share; }
    long minorFaults = 0;

    /**
     * Get the non i/o page faults.<p>
     * Supported Platforms: AIX, HPUX, Linux, Solaris.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return non i/o page faults
     */
    public long getMinorFaults() { return minorFaults; }
    long majorFaults = 0;

    /**
     * Get the i/o page faults.<p>
     * Supported Platforms: AIX, HPUX, Linux, Solaris.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return i/o page faults
     */
    public long getMajorFaults() { return majorFaults; }
    long pageFaults = 0;

    /**
     * Get the Total number of page faults.<p>
     * Supported Platforms: AIX, Darwin, HPUX, Linux, Solaris, Win32.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code>top, ps</code><br>
     * <li> Darwin: <code>top, ps</code><br>
     * <li> FreeBSD: <code>top, ps</code><br>
     * <li> HPUX: <code>top, ps</code><br>
     * <li> Linux: <code>top, ps</code><br>
     * <li> Solaris: <code>top, ps</code><br>
     * <li> Win32: <code>taskman</code><br>
     * </ul>
     * @return Total number of page faults
     */
    public long getPageFaults() { return pageFaults; }

    void copyTo(ProcMem copy) {
        copy.size = this.size;
        copy.resident = this.resident;
        copy.share = this.share;
        copy.minorFaults = this.minorFaults;
        copy.majorFaults = this.majorFaults;
        copy.pageFaults = this.pageFaults;
    }
    /**
     * @deprecated
     * @see #getResident()
     */
    public long getRss() { return getResident(); }
    /**
     * @deprecated
     * @see #getSize()
     */
    public long getVsize() { return getSize(); }

    public Map toMap() {
        Map map = new HashMap();
        String strsize = 
            String.valueOf(this.size);
        if (!"-1".equals(strsize))
            map.put("Size", strsize);
        String strresident = 
            String.valueOf(this.resident);
        if (!"-1".equals(strresident))
            map.put("Resident", strresident);
        String strshare = 
            String.valueOf(this.share);
        if (!"-1".equals(strshare))
            map.put("Share", strshare);
        String strminorFaults = 
            String.valueOf(this.minorFaults);
        if (!"-1".equals(strminorFaults))
            map.put("MinorFaults", strminorFaults);
        String strmajorFaults = 
            String.valueOf(this.majorFaults);
        if (!"-1".equals(strmajorFaults))
            map.put("MajorFaults", strmajorFaults);
        String strpageFaults = 
            String.valueOf(this.pageFaults);
        if (!"-1".equals(strpageFaults))
            map.put("PageFaults", strpageFaults);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}