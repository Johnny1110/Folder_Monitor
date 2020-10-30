package com.frizo.lib.foldermonitor.storage.model;

import java.io.Serializable;
import java.time.Instant;

public class FolderFile implements Serializable, Comparable<FolderFile>{

    private String monitorName;

    private String filename;

    private int readLine;

    private String hashCode;

    private Instant createdAt;

    private Instant updatedAt;

    private String parentPackName;

    private Instant logDate;

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getReadLine() {
        return readLine;
    }

    public void setReadLine(int readLine) {
        this.readLine = readLine;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getParentPackName() {
        return parentPackName;
    }

    public void setParentPackName(String parentPackName) {
        this.parentPackName = parentPackName;
    }

    public Instant getLogDate() {
        return logDate;
    }

    public void setLogDate(Instant logDate) {
        this.logDate = logDate;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------------\n")
                .append("filename : " + filename)
                .append("\n")
                .append("readLine : " + readLine)
                .append("\n")
                .append("hashCode : " + hashCode)
                .append("\n")
                .append("createdAt: " + createdAt)
                .append("\n")
                .append("updatedAt: " + updatedAt)
                .append("\n")
                .append("monitorName : " + monitorName)
                .append("\n")
                .append("parentPackName : " + parentPackName)
                .append("\n")
                .append("logDate : " + logDate)
                .append("\n");
        sb.append("------------------------------------------------");
        return sb.toString();
    }

    @Override
    public int compareTo(FolderFile other) {
        long ans = this.getLogDate().toEpochMilli() - other.getLogDate().toEpochMilli();

        if (ans > 0){
            return 1;
        }else if(ans < 0){
            return -1;
        }else {
            return this.filename.compareTo(other.filename);
        }
    }
}
