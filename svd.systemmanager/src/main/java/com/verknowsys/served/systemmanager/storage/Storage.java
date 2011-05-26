package com.verknowsys.served.systemmanager.storage;

import java.sql.*;
import java.util.List;
import java.util.Date;
import java.sql.Timestamp;
import java.io.File;

// TODO: Add parent pid field to ProcessInfo

public class Storage {
    private Connection conn = null;
    private PreparedStatement insertStatement = null;
    private PreparedStatement avgCpuByPIDStatement = null;
    private PreparedStatement avgCpuByPIDAndTimeStatement = null;
    private PreparedStatement avgCpuByNameStatement = null;
    private PreparedStatement avgCpuByNameAndTimeStatement = null;
    private PreparedStatement avgCpuByTimeStatement = null;
    
    public Storage(String databaseFilePath) throws SQLException {
        // Check if database file exist, if not, setup processinfo table
        boolean doSetup = !(new File(databaseFilePath)).exists();
        conn = DriverManager.getConnection("jdbc:h2:" + databaseFilePath);
        if(doSetup) setupTable();
        
        insertStatement = conn.prepareStatement("INSERT INTO processinfo VALUES (?, ?, ?, ?, ?);");
        avgCpuByPIDStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE pid = ?;");
        avgCpuByPIDAndTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE pid = ? AND time BETWEEN ? AND ?;");
        avgCpuByNameStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE name = ?;");
        avgCpuByNameAndTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE name = ? AND time BETWEEN ? AND ?;");
        avgCpuByTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE time BETWEEN ? AND ?;");
    }
    
    private void setupTable() throws SQLException {
        conn.createStatement().executeUpdate("SET CACHE_SIZE 0;");
        conn.createStatement().executeUpdate("DROP TABLE IF EXISTS processinfo;");
        conn.createStatement().executeUpdate("CREATE TABLE processinfo (" +
            "pid INTEGER NOT NULL," +
            "name VARCHAR(50) NOT NULL," +
            "cpu INTEGER NOT NULL," +
            "mem INTEGER NOT NULL," +
            "time TIMESTAMP NOT NULL" +
        ");");
        conn.commit();
    }
    
    public void save(ProcessInfo processInfo) throws SQLException {
        insertStatement.setInt(1, processInfo.pid);
        insertStatement.setString(2, processInfo.name);
        insertStatement.setInt(3, processInfo.cpu);
        insertStatement.setInt(4, processInfo.mem);
        insertStatement.setTimestamp(5, processInfo.time);
        insertStatement.execute();
    }
    
    // public List<ProcessInfo> getByPID(int pid);
    // public List<ProcessInfo> getByPIDAndTime(int pid, Timestamp from, Timestamp to);
    // public List<ProcessInfo> getByName(String name);
    // public List<ProcessInfo> getByNameAndTime(String name, Timestamp from, Timestamp to);
    // public List<ProcessInfo> getByTime(Timestamp from, Timestamp to);
    
    // public int sumCpuByPID(int pid);
    // public int sumCpuByPIDAndTime(int pid, Timestamp from, Timestamp to);
    // public int sumCpuByName(String name);
    // public int sumCpuByNameAndTime(String name, Timestamp from, Timestamp to);
    // public int sumCpuByTime(Timestamp from, Timestamp to);
    
    /**
     * Returns average CPU usage for specified PID
     * @author teamon
     */
    public float avgCpuByPID(int pid) throws SQLException {
        avgCpuByPIDStatement.setInt(1, pid);
        ResultSet results = avgCpuByPIDStatement.executeQuery();
        results.next();
        return results.getFloat("avg_cpu");
    }
    
    /**
     * Returns average CPU usage for specified PID and time range
     * @author teamon
     */
    public float avgCpuByPIDAndTime(int pid, Timestamp from, Timestamp to) throws SQLException {
        avgCpuByPIDAndTimeStatement.setInt(1, pid);
        avgCpuByPIDAndTimeStatement.setTimestamp(2, from);
        avgCpuByPIDAndTimeStatement.setTimestamp(3, to);
        ResultSet results = avgCpuByPIDAndTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_cpu");
    }
    
    /**
     * Returns average CPU usage for specified process name
     * @author teamon
     */
    public float avgCpuByName(String name) throws SQLException {
        avgCpuByNameStatement.setString(1, name);
        ResultSet results = avgCpuByNameStatement.executeQuery();
        results.next();
        return results.getFloat("avg_cpu");
    }
    
    /**
     * Returns average CPU usage for specified process name and time range
     * @author teamon
     */
    public float avgCpuByNameAndTime(String name, Timestamp from, Timestamp to) throws SQLException {
        avgCpuByNameAndTimeStatement.setString(1, name);
        avgCpuByNameAndTimeStatement.setTimestamp(2, from);
        avgCpuByNameAndTimeStatement.setTimestamp(3, to);
        ResultSet results = avgCpuByNameAndTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_cpu");
    }
    
    /**
     * Returns average CPU usage for all processes within specified time range
     * @author teamon
     */
    public float avgCpuByTime(Timestamp from, Timestamp to) throws SQLException {
        avgCpuByTimeStatement.setTimestamp(1, from);
        avgCpuByTimeStatement.setTimestamp(2, to);
        ResultSet results = avgCpuByTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_cpu");
    }
    
    // public int sumMemByPID(int pid);
    // public int sumMemByPIDAndTime(int pid, Timestamp from, Timestamp to);
    // public int sumMemByName(String name);
    // public int sumMemByNameAndTime(String name, Timestamp from, Timestamp to);
    // public int sumMemByTime(Timestamp from, Timestamp to);
    
    // public float avgMemByPID(int pid);
    // public float avgMemByPIDAndTime(int pid, Timestamp from, Timestamp to);
    // public float avgMemByName(String name);
    // public float avgMemByNameAndTime(String name, Timestamp from, Timestamp to);
    // public float avgMemByTime(Timestamp from, Timestamp to);
}
