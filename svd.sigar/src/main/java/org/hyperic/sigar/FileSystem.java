/*****************************************************
 * WARNING: this file was generated by -e
 * on Sat Mar  5 03:11:36 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * FileSystem sigar class.
 */
public class FileSystem implements java.io.Serializable {

    private static final long serialVersionUID = 9641L;

    public FileSystem() { }

    public native void gather(Sigar sigar) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getFileSystem() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getFileSystem
     */
    static FileSystem fetch(Sigar sigar) throws SigarException {
        FileSystem fileSystem = new FileSystem();
        fileSystem.gather(sigar);
        return fileSystem;
    }

    String dirName = null;

    /**
     * Get the Directory name.<p>
     * Supported Platforms: All.
     * <p>
     * @return Directory name
     */
    public String getDirName() { return dirName; }
    String devName = null;

    /**
     * Get the Device name.<p>
     * Supported Platforms: All.
     * <p>
     * @return Device name
     */
    public String getDevName() { return devName; }
    String typeName = null;

    /**
     * Get the File system generic type name.<p>
     * Supported Platforms: All.
     * <p>
     * @return File system generic type name
     */
    public String getTypeName() { return typeName; }
    String sysTypeName = null;

    /**
     * Get the File system os specific type name.<p>
     * Supported Platforms: All.
     * <p>
     * @return File system os specific type name
     */
    public String getSysTypeName() { return sysTypeName; }
    String options = null;

    /**
     * Get the File system mount options.<p>
     * Supported Platforms: All.
     * <p>
     * @return File system mount options
     */
    public String getOptions() { return options; }
    int type = 0;

    /**
     * Get the File system type.<p>
     * Supported Platforms: All.
     * <p>
     * @return File system type
     */
    public int getType() { return type; }
    long flags = 0;

    /**
     * Get the File system flags.<p>
     * Supported Platforms: All.
     * <p>
     * @return File system flags
     */
    public long getFlags() { return flags; }

    void copyTo(FileSystem copy) {
        copy.dirName = this.dirName;
        copy.devName = this.devName;
        copy.typeName = this.typeName;
        copy.sysTypeName = this.sysTypeName;
        copy.options = this.options;
        copy.type = this.type;
        copy.flags = this.flags;
    }
    public static final int TYPE_UNKNOWN    = 0;
    public static final int TYPE_NONE       = 1;
    public static final int TYPE_LOCAL_DISK = 2;
    public static final int TYPE_NETWORK    = 3;
    public static final int TYPE_RAM_DISK   = 4;
    public static final int TYPE_CDROM      = 5;
    public static final int TYPE_SWAP       = 6;

    public String toString() {
        return this.getDirName();
    }

    public Map toMap() {
        Map map = new HashMap();
        String strdirName = 
            String.valueOf(this.dirName);
        if (!"-1".equals(strdirName))
            map.put("DirName", strdirName);
        String strdevName = 
            String.valueOf(this.devName);
        if (!"-1".equals(strdevName))
            map.put("DevName", strdevName);
        String strtypeName = 
            String.valueOf(this.typeName);
        if (!"-1".equals(strtypeName))
            map.put("TypeName", strtypeName);
        String strsysTypeName = 
            String.valueOf(this.sysTypeName);
        if (!"-1".equals(strsysTypeName))
            map.put("SysTypeName", strsysTypeName);
        String stroptions = 
            String.valueOf(this.options);
        if (!"-1".equals(stroptions))
            map.put("Options", stroptions);
        String strtype = 
            String.valueOf(this.type);
        if (!"-1".equals(strtype))
            map.put("Type", strtype);
        String strflags = 
            String.valueOf(this.flags);
        if (!"-1".equals(strflags))
            map.put("Flags", strflags);
        return map;
    }

}
