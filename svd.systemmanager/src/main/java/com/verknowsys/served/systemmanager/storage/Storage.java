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
    
    private PreparedStatement sumCpuByPIDStatement = null;
    private PreparedStatement sumCpuByPIDAndTimeStatement = null;
    private PreparedStatement sumCpuByNameStatement = null;
    private PreparedStatement sumCpuByNameAndTimeStatement = null;
    private PreparedStatement sumCpuByTimeStatement = null;
    
    private PreparedStatement avgCpuByPIDStatement = null;
    private PreparedStatement avgCpuByPIDAndTimeStatement = null;
    private PreparedStatement avgCpuByNameStatement = null;
    private PreparedStatement avgCpuByNameAndTimeStatement = null;
    private PreparedStatement avgCpuByTimeStatement = null;
    
    private PreparedStatement sumMemByPIDStatement = null;
    private PreparedStatement sumMemByPIDAndTimeStatement = null;
    private PreparedStatement sumMemByNameStatement = null;
    private PreparedStatement sumMemByNameAndTimeStatement = null;
    private PreparedStatement sumMemByTimeStatement = null;
    
    private PreparedStatement avgMemByPIDStatement = null;
    private PreparedStatement avgMemByPIDAndTimeStatement = null;
    private PreparedStatement avgMemByNameStatement = null;
    private PreparedStatement avgMemByNameAndTimeStatement = null;
    private PreparedStatement avgMemByTimeStatement = null;
    
    
    public Storage(String databaseFilePath) throws SQLException {
        // Check if database file exist, if not, setup processinfo table
        boolean doSetup = !(new File(databaseFilePath)).exists();
        conn = DriverManager.getConnection("jdbc:h2:" + databaseFilePath);
        if(doSetup) setupTable();
        
        insertStatement = conn.prepareStatement("INSERT INTO processinfo VALUES (?, ?, ?, ?, ?);");
        
        sumCpuByPIDStatement = conn.prepareStatement("SELECT SUM(cpu) AS sum_cpu FROM processinfo WHERE pid = ?;");
        sumCpuByPIDAndTimeStatement = conn.prepareStatement("SELECT SUM(cpu) AS sum_cpu FROM processinfo WHERE pid = ? AND time BETWEEN ? AND ?;");
        sumCpuByNameStatement = conn.prepareStatement("SELECT SUM(cpu) AS sum_cpu FROM processinfo WHERE name = ?;");
        sumCpuByNameAndTimeStatement = conn.prepareStatement("SELECT SUM(cpu) AS sum_cpu FROM processinfo WHERE name = ? AND time BETWEEN ? AND ?;");
        sumCpuByTimeStatement = conn.prepareStatement("SELECT SUM(cpu) AS sum_cpu FROM processinfo WHERE time BETWEEN ? AND ?;");
        
        avgCpuByPIDStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE pid = ?;");
        avgCpuByPIDAndTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE pid = ? AND time BETWEEN ? AND ?;");
        avgCpuByNameStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE name = ?;");
        avgCpuByNameAndTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE name = ? AND time BETWEEN ? AND ?;");
        avgCpuByTimeStatement = conn.prepareStatement("SELECT AVG(cpu) AS avg_cpu FROM processinfo WHERE time BETWEEN ? AND ?;");

        sumMemByPIDStatement = conn.prepareStatement("SELECT SUM(mem) AS sum_mem FROM processinfo WHERE pid = ?;");
        sumMemByPIDAndTimeStatement = conn.prepareStatement("SELECT SUM(mem) AS sum_mem FROM processinfo WHERE pid = ? AND time BETWEEN ? AND ?;");
        sumMemByNameStatement = conn.prepareStatement("SELECT SUM(mem) AS sum_mem FROM processinfo WHERE name = ?;");
        sumMemByNameAndTimeStatement = conn.prepareStatement("SELECT SUM(mem) AS sum_mem FROM processinfo WHERE name = ? AND time BETWEEN ? AND ?;");
        sumMemByTimeStatement = conn.prepareStatement("SELECT SUM(mem) AS sum_mem FROM processinfo WHERE time BETWEEN ? AND ?;");

        avgMemByPIDStatement = conn.prepareStatement("SELECT AVG(mem) AS avg_mem FROM processinfo WHERE pid = ?;");
        avgMemByPIDAndTimeStatement = conn.prepareStatement("SELECT AVG(mem) AS avg_mem FROM processinfo WHERE pid = ? AND time BETWEEN ? AND ?;");
        avgMemByNameStatement = conn.prepareStatement("SELECT AVG(mem) AS avg_mem FROM processinfo WHERE name = ?;");
        avgMemByNameAndTimeStatement = conn.prepareStatement("SELECT AVG(mem) AS avg_mem FROM processinfo WHERE name = ? AND time BETWEEN ? AND ?;");
        avgMemByTimeStatement = conn.prepareStatement("SELECT AVG(mem) AS avg_mem FROM processinfo WHERE time BETWEEN ? AND ?;");

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

    /**
     * Returns total of CPU usage for specified PID
     * @author teamon
     */
    public int sumCpuByPID(int pid) throws SQLException {
        sumCpuByPIDStatement.setInt(1, pid);
        ResultSet results = sumCpuByPIDStatement.executeQuery();
        results.next();
        return results.getInt("sum_cpu");
    }

    /**
     * Returns total of CPU usage for specified PID and time range
     * @author teamon
     */
    public int sumCpuByPIDAndTime(int pid, Timestamp from, Timestamp to) throws SQLException {
        sumCpuByPIDAndTimeStatement.setInt(1, pid);
        sumCpuByPIDAndTimeStatement.setTimestamp(2, from);
        sumCpuByPIDAndTimeStatement.setTimestamp(3, to);
        ResultSet results = sumCpuByPIDAndTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_cpu");
    }

    /**
     * Returns total of CPU usage for specified process name
     * @author teamon
     */
    public int sumCpuByName(String name) throws SQLException {
        sumCpuByNameStatement.setString(1, name);
        ResultSet results = sumCpuByNameStatement.executeQuery();
        results.next();
        return results.getInt("sum_cpu");
    }

    /**
     * Returns total of CPU usage for specified process name and time range
     * @author teamon
     */
    public int sumCpuByNameAndTime(String name, Timestamp from, Timestamp to) throws SQLException {
        sumCpuByNameAndTimeStatement.setString(1, name);
        sumCpuByNameAndTimeStatement.setTimestamp(2, from);
        sumCpuByNameAndTimeStatement.setTimestamp(3, to);
        ResultSet results = sumCpuByNameAndTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_cpu");
    }

    /**
     * Returns total of CPU usage within specified time range
     * @author teamon
     */
    public int sumCpuByTime(Timestamp from, Timestamp to) throws SQLException {
        sumCpuByTimeStatement.setTimestamp(1, from);
        sumCpuByTimeStatement.setTimestamp(2, to);
        ResultSet results = sumCpuByTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_cpu");
    }
    
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
    
    /**
     * Returns total of memory usage for specified PID
     * @author teamon
     */
    public int sumMemByPID(int pid) throws SQLException {
        sumMemByPIDStatement.setInt(1, pid);
        ResultSet results = sumMemByPIDStatement.executeQuery();
        results.next();
        return results.getInt("sum_mem");
    }
    
    /**
     * Returns total of memory usage for specified PID and time range
     * @author teamon
     */
    public int sumMemByPIDAndTime(int pid, Timestamp from, Timestamp to) throws SQLException {
        sumMemByPIDAndTimeStatement.setInt(1, pid);
        sumMemByPIDAndTimeStatement.setTimestamp(2, from);
        sumMemByPIDAndTimeStatement.setTimestamp(3, to);
        ResultSet results = sumMemByPIDAndTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_mem");
    }
    
    /**
     * Returns total of memory usage for specified process name
     * @author teamon
     */
    public int sumMemByName(String name) throws SQLException {
        sumMemByNameStatement.setString(1, name);
        ResultSet results = sumMemByNameStatement.executeQuery();
        results.next();
        return results.getInt("sum_mem");
    }
    
    /**
     * Returns total of memory usage for specified process name and time range
     * @author teamon
     */
    public int sumMemByNameAndTime(String name, Timestamp from, Timestamp to) throws SQLException {
        sumMemByNameAndTimeStatement.setString(1, name);
        sumMemByNameAndTimeStatement.setTimestamp(2, from);
        sumMemByNameAndTimeStatement.setTimestamp(3, to);
        ResultSet results = sumMemByNameAndTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_mem");
    }
    
    /**
     * Returns total of memory usage within specified time range
     * @author teamon
     */
    public int sumMemByTime(Timestamp from, Timestamp to) throws SQLException {
        sumMemByTimeStatement.setTimestamp(1, from);
        sumMemByTimeStatement.setTimestamp(2, to);
        ResultSet results = sumMemByTimeStatement.executeQuery();
        results.next();
        return results.getInt("sum_mem");
    }
    
    /**
     * Returns average memory usage for specified PID
     * @author teamon
     */
    public float avgMemByPID(int pid) throws SQLException {
        avgMemByPIDStatement.setInt(1, pid);
        ResultSet results = avgMemByPIDStatement.executeQuery();
        results.next();
        return results.getFloat("avg_mem");
    }

    /**
     * Returns average memory usage for specified PID and time range
     * @author teamon
     */
    public float avgMemByPIDAndTime(int pid, Timestamp from, Timestamp to) throws SQLException {
        avgMemByPIDAndTimeStatement.setInt(1, pid);
        avgMemByPIDAndTimeStatement.setTimestamp(2, from);
        avgMemByPIDAndTimeStatement.setTimestamp(3, to);
        ResultSet results = avgMemByPIDAndTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_mem");
    }

    /**
     * Returns average memory usage for specified process name
     * @author teamon
     */
    public float avgMemByName(String name) throws SQLException {
        avgMemByNameStatement.setString(1, name);
        ResultSet results = avgMemByNameStatement.executeQuery();
        results.next();
        return results.getFloat("avg_mem");
    }

    /**
     * Returns average memory usage for specified process name and time range
     * @author teamon
     */
    public float avgMemByNameAndTime(String name, Timestamp from, Timestamp to) throws SQLException {
        avgMemByNameAndTimeStatement.setString(1, name);
        avgMemByNameAndTimeStatement.setTimestamp(2, from);
        avgMemByNameAndTimeStatement.setTimestamp(3, to);
        ResultSet results = avgMemByNameAndTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_mem");
    }

    /**
     * Returns average memory usage for all processes within specified time range
     * @author teamon
     */
    public float avgMemByTime(Timestamp from, Timestamp to) throws SQLException {
        avgMemByTimeStatement.setTimestamp(1, from);
        avgMemByTimeStatement.setTimestamp(2, to);
        ResultSet results = avgMemByTimeStatement.executeQuery();
        results.next();
        return results.getFloat("avg_mem");
    }
}
