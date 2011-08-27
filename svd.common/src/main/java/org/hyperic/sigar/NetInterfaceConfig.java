/*****************************************************
 * WARNING: this file was generated by -e
 * on Thu Jun  9 04:13:13 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * NetInterfaceConfig sigar class.
 */
public class NetInterfaceConfig implements java.io.Serializable {

    private static final long serialVersionUID = 15948L;

    public NetInterfaceConfig() { }

    public native void gather(Sigar sigar, String name) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getNetInterfaceConfig() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getNetInterfaceConfig
     */
    static NetInterfaceConfig fetch(Sigar sigar, String name) throws SigarException {
        NetInterfaceConfig netInterfaceConfig = new NetInterfaceConfig();
        netInterfaceConfig.gather(sigar, name);
        return netInterfaceConfig;
    }

    String name = null;

    /**
     * Get the name.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return name
     */
    public String getName() { return name; }
    String hwaddr = null;

    /**
     * Get the hwaddr.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return hwaddr
     */
    public String getHwaddr() { return hwaddr; }
    String type = null;

    /**
     * Get the type.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return type
     */
    public String getType() { return type; }
    String description = null;

    /**
     * Get the description.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return description
     */
    public String getDescription() { return description; }
    String address = null;

    /**
     * Get the address.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return address
     */
    public String getAddress() { return address; }
    String destination = null;

    /**
     * Get the destination.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return destination
     */
    public String getDestination() { return destination; }
    String broadcast = null;

    /**
     * Get the broadcast.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return broadcast
     */
    public String getBroadcast() { return broadcast; }
    String netmask = null;

    /**
     * Get the netmask.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return netmask
     */
    public String getNetmask() { return netmask; }
    long flags = 0;

    /**
     * Get the flags.<p>
     * Supported Platforms: All.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return flags
     */
    public long getFlags() { return flags; }
    long mtu = 0;

    /**
     * Get the mtu.<p>
     * Supported Platforms: Darwin, FreeBSD, Linux.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return mtu
     */
    public long getMtu() { return mtu; }
    long metric = 0;

    /**
     * Get the metric.<p>
     * Supported Platforms: Darwin, FreeBSD, Linux.
     * <p>
     * System equivalent commands:<ul>
     * <li> AIX: <code></code><br>
     * <li> Darwin: <code></code><br>
     * <li> FreeBSD: <code></code><br>
     * <li> HPUX: <code></code><br>
     * <li> Linux: <code>ifconfig</code><br>
     * <li> Solaris: <code>ifconfig -a</code><br>
     * <li> Win32: <code></code><br>
     * </ul>
     * @return metric
     */
    public long getMetric() { return metric; }

    void copyTo(NetInterfaceConfig copy) {
        copy.name = this.name;
        copy.hwaddr = this.hwaddr;
        copy.type = this.type;
        copy.description = this.description;
        copy.address = this.address;
        copy.destination = this.destination;
        copy.broadcast = this.broadcast;
        copy.netmask = this.netmask;
        copy.flags = this.flags;
        copy.mtu = this.mtu;
        copy.metric = this.metric;
    }

    public Map toMap() {
        Map map = new HashMap();
        String strname = 
            String.valueOf(this.name);
        if (!"-1".equals(strname))
            map.put("Name", strname);
        String strhwaddr = 
            String.valueOf(this.hwaddr);
        if (!"-1".equals(strhwaddr))
            map.put("Hwaddr", strhwaddr);
        String strtype = 
            String.valueOf(this.type);
        if (!"-1".equals(strtype))
            map.put("Type", strtype);
        String strdescription = 
            String.valueOf(this.description);
        if (!"-1".equals(strdescription))
            map.put("Description", strdescription);
        String straddress = 
            String.valueOf(this.address);
        if (!"-1".equals(straddress))
            map.put("Address", straddress);
        String strdestination = 
            String.valueOf(this.destination);
        if (!"-1".equals(strdestination))
            map.put("Destination", strdestination);
        String strbroadcast = 
            String.valueOf(this.broadcast);
        if (!"-1".equals(strbroadcast))
            map.put("Broadcast", strbroadcast);
        String strnetmask = 
            String.valueOf(this.netmask);
        if (!"-1".equals(strnetmask))
            map.put("Netmask", strnetmask);
        String strflags = 
            String.valueOf(this.flags);
        if (!"-1".equals(strflags))
            map.put("Flags", strflags);
        String strmtu = 
            String.valueOf(this.mtu);
        if (!"-1".equals(strmtu))
            map.put("Mtu", strmtu);
        String strmetric = 
            String.valueOf(this.metric);
        if (!"-1".equals(strmetric))
            map.put("Metric", strmetric);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}