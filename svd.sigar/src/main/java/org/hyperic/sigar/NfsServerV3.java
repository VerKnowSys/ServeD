/*****************************************************
 * WARNING: this file was generated by -e
 * on Sat Mar  5 03:11:36 2011.
 * Any changes made here will be LOST.
 *****************************************************/
package org.hyperic.sigar;

import java.util.HashMap;
import java.util.Map;

/**
 * NfsServerV3 sigar class.
 */
public class NfsServerV3 implements java.io.Serializable {

    private static final long serialVersionUID = 23335L;

    public NfsServerV3() { }

    public native void gather(Sigar sigar) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getNfsServerV3() instead.
     * @exception SigarException on failure.
     * @see org.hyperic.sigar.Sigar#getNfsServerV3
     */
    static NfsServerV3 fetch(Sigar sigar) throws SigarException {
        NfsServerV3 nfsServerV3 = new NfsServerV3();
        nfsServerV3.gather(sigar);
        return nfsServerV3;
    }

    long _null = 0;

    /**
     * Get the null.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return null
     */
    public long getNull() { return _null; }
    long getattr = 0;

    /**
     * Get the getattr.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return getattr
     */
    public long getGetattr() { return getattr; }
    long setattr = 0;

    /**
     * Get the setattr.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return setattr
     */
    public long getSetattr() { return setattr; }
    long lookup = 0;

    /**
     * Get the lookup.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return lookup
     */
    public long getLookup() { return lookup; }
    long access = 0;

    /**
     * Get the access.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return access
     */
    public long getAccess() { return access; }
    long readlink = 0;

    /**
     * Get the readlink.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return readlink
     */
    public long getReadlink() { return readlink; }
    long read = 0;

    /**
     * Get the read.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return read
     */
    public long getRead() { return read; }
    long write = 0;

    /**
     * Get the write.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return write
     */
    public long getWrite() { return write; }
    long create = 0;

    /**
     * Get the create.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return create
     */
    public long getCreate() { return create; }
    long mkdir = 0;

    /**
     * Get the mkdir.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return mkdir
     */
    public long getMkdir() { return mkdir; }
    long symlink = 0;

    /**
     * Get the symlink.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return symlink
     */
    public long getSymlink() { return symlink; }
    long mknod = 0;

    /**
     * Get the mknod.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return mknod
     */
    public long getMknod() { return mknod; }
    long remove = 0;

    /**
     * Get the remove.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return remove
     */
    public long getRemove() { return remove; }
    long rmdir = 0;

    /**
     * Get the rmdir.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return rmdir
     */
    public long getRmdir() { return rmdir; }
    long rename = 0;

    /**
     * Get the rename.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return rename
     */
    public long getRename() { return rename; }
    long link = 0;

    /**
     * Get the link.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return link
     */
    public long getLink() { return link; }
    long readdir = 0;

    /**
     * Get the readdir.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return readdir
     */
    public long getReaddir() { return readdir; }
    long readdirplus = 0;

    /**
     * Get the readdirplus.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return readdirplus
     */
    public long getReaddirplus() { return readdirplus; }
    long fsstat = 0;

    /**
     * Get the fsstat.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return fsstat
     */
    public long getFsstat() { return fsstat; }
    long fsinfo = 0;

    /**
     * Get the fsinfo.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return fsinfo
     */
    public long getFsinfo() { return fsinfo; }
    long pathconf = 0;

    /**
     * Get the pathconf.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return pathconf
     */
    public long getPathconf() { return pathconf; }
    long commit = 0;

    /**
     * Get the commit.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return commit
     */
    public long getCommit() { return commit; }

    void copyTo(NfsServerV3 copy) {
        copy._null = this._null;
        copy.getattr = this.getattr;
        copy.setattr = this.setattr;
        copy.lookup = this.lookup;
        copy.access = this.access;
        copy.readlink = this.readlink;
        copy.read = this.read;
        copy.write = this.write;
        copy.create = this.create;
        copy.mkdir = this.mkdir;
        copy.symlink = this.symlink;
        copy.mknod = this.mknod;
        copy.remove = this.remove;
        copy.rmdir = this.rmdir;
        copy.rename = this.rename;
        copy.link = this.link;
        copy.readdir = this.readdir;
        copy.readdirplus = this.readdirplus;
        copy.fsstat = this.fsstat;
        copy.fsinfo = this.fsinfo;
        copy.pathconf = this.pathconf;
        copy.commit = this.commit;
    }

    public Map toMap() {
        Map map = new HashMap();
        String str_null = 
            String.valueOf(this._null);
        if (!"-1".equals(str_null))
            map.put("_null", str_null);
        String strgetattr = 
            String.valueOf(this.getattr);
        if (!"-1".equals(strgetattr))
            map.put("Getattr", strgetattr);
        String strsetattr = 
            String.valueOf(this.setattr);
        if (!"-1".equals(strsetattr))
            map.put("Setattr", strsetattr);
        String strlookup = 
            String.valueOf(this.lookup);
        if (!"-1".equals(strlookup))
            map.put("Lookup", strlookup);
        String straccess = 
            String.valueOf(this.access);
        if (!"-1".equals(straccess))
            map.put("Access", straccess);
        String strreadlink = 
            String.valueOf(this.readlink);
        if (!"-1".equals(strreadlink))
            map.put("Readlink", strreadlink);
        String strread = 
            String.valueOf(this.read);
        if (!"-1".equals(strread))
            map.put("Read", strread);
        String strwrite = 
            String.valueOf(this.write);
        if (!"-1".equals(strwrite))
            map.put("Write", strwrite);
        String strcreate = 
            String.valueOf(this.create);
        if (!"-1".equals(strcreate))
            map.put("Create", strcreate);
        String strmkdir = 
            String.valueOf(this.mkdir);
        if (!"-1".equals(strmkdir))
            map.put("Mkdir", strmkdir);
        String strsymlink = 
            String.valueOf(this.symlink);
        if (!"-1".equals(strsymlink))
            map.put("Symlink", strsymlink);
        String strmknod = 
            String.valueOf(this.mknod);
        if (!"-1".equals(strmknod))
            map.put("Mknod", strmknod);
        String strremove = 
            String.valueOf(this.remove);
        if (!"-1".equals(strremove))
            map.put("Remove", strremove);
        String strrmdir = 
            String.valueOf(this.rmdir);
        if (!"-1".equals(strrmdir))
            map.put("Rmdir", strrmdir);
        String strrename = 
            String.valueOf(this.rename);
        if (!"-1".equals(strrename))
            map.put("Rename", strrename);
        String strlink = 
            String.valueOf(this.link);
        if (!"-1".equals(strlink))
            map.put("Link", strlink);
        String strreaddir = 
            String.valueOf(this.readdir);
        if (!"-1".equals(strreaddir))
            map.put("Readdir", strreaddir);
        String strreaddirplus = 
            String.valueOf(this.readdirplus);
        if (!"-1".equals(strreaddirplus))
            map.put("Readdirplus", strreaddirplus);
        String strfsstat = 
            String.valueOf(this.fsstat);
        if (!"-1".equals(strfsstat))
            map.put("Fsstat", strfsstat);
        String strfsinfo = 
            String.valueOf(this.fsinfo);
        if (!"-1".equals(strfsinfo))
            map.put("Fsinfo", strfsinfo);
        String strpathconf = 
            String.valueOf(this.pathconf);
        if (!"-1".equals(strpathconf))
            map.put("Pathconf", strpathconf);
        String strcommit = 
            String.valueOf(this.commit);
        if (!"-1".equals(strcommit))
            map.put("Commit", strcommit);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}
