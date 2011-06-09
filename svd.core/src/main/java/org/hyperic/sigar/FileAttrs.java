/*****************************************************
 * WARNING: this file was generated by -e
 * on Thu Jun  9 04:13:13 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * FileAttrs sigar class.
 */
public class FileAttrs implements java.io.Serializable {

    private static final long serialVersionUID = 10323L;

    public FileAttrs() { }

    public native void gather(Sigar sigar, String name) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getFileAttrs() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getFileAttrs
     */
    static FileAttrs fetch(Sigar sigar, String name) throws SigarException {
        FileAttrs fileAttrs = new FileAttrs();
        fileAttrs.gather(sigar, name);
        return fileAttrs;
    }

    long permissions = 0;

    /**
     * Get the permissions.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return permissions
     */
    public long getPermissions() { return permissions; }
    int type = 0;

    /**
     * Get the type.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return type
     */
    public int getType() { return type; }
    long uid = 0;

    /**
     * Get the uid.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return uid
     */
    public long getUid() { return uid; }
    long gid = 0;

    /**
     * Get the gid.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return gid
     */
    public long getGid() { return gid; }
    long inode = 0;

    /**
     * Get the inode.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return inode
     */
    public long getInode() { return inode; }
    long device = 0;

    /**
     * Get the device.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return device
     */
    public long getDevice() { return device; }
    long nlink = 0;

    /**
     * Get the nlink.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return nlink
     */
    public long getNlink() { return nlink; }
    long size = 0;

    /**
     * Get the size.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return size
     */
    public long getSize() { return size; }
    long atime = 0;

    /**
     * Get the atime.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return atime
     */
    public long getAtime() { return atime; }
    long ctime = 0;

    /**
     * Get the ctime.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return ctime
     */
    public long getCtime() { return ctime; }
    long mtime = 0;

    /**
     * Get the mtime.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return mtime
     */
    public long getMtime() { return mtime; }

    void copyTo(FileAttrs copy) {
        copy.permissions = this.permissions;
        copy.type = this.type;
        copy.uid = this.uid;
        copy.gid = this.gid;
        copy.inode = this.inode;
        copy.device = this.device;
        copy.nlink = this.nlink;
        copy.size = this.size;
        copy.atime = this.atime;
        copy.ctime = this.ctime;
        copy.mtime = this.mtime;
    }

    public Map toMap() {
        Map map = new HashMap();
        String strpermissions = 
            String.valueOf(this.permissions);
        if (!"-1".equals(strpermissions))
            map.put("Permissions", strpermissions);
        String strtype = 
            String.valueOf(this.type);
        if (!"-1".equals(strtype))
            map.put("Type", strtype);
        String struid = 
            String.valueOf(this.uid);
        if (!"-1".equals(struid))
            map.put("Uid", struid);
        String strgid = 
            String.valueOf(this.gid);
        if (!"-1".equals(strgid))
            map.put("Gid", strgid);
        String strinode = 
            String.valueOf(this.inode);
        if (!"-1".equals(strinode))
            map.put("Inode", strinode);
        String strdevice = 
            String.valueOf(this.device);
        if (!"-1".equals(strdevice))
            map.put("Device", strdevice);
        String strnlink = 
            String.valueOf(this.nlink);
        if (!"-1".equals(strnlink))
            map.put("Nlink", strnlink);
        String strsize = 
            String.valueOf(this.size);
        if (!"-1".equals(strsize))
            map.put("Size", strsize);
        String stratime = 
            String.valueOf(this.atime);
        if (!"-1".equals(stratime))
            map.put("Atime", stratime);
        String strctime = 
            String.valueOf(this.ctime);
        if (!"-1".equals(strctime))
            map.put("Ctime", strctime);
        String strmtime = 
            String.valueOf(this.mtime);
        if (!"-1".equals(strmtime))
            map.put("Mtime", strmtime);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}
