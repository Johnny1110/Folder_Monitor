package com.frizo.lib.foldermonitor.storage.model;

import java.time.Instant;

public class MonitorJob {

    private String monitorName;

    private String folderPath;

    private Instant createdAt;

    private String filenamePrefix;

    private String fileNameSuffix;

    private String regexStr;

    private String headFilename;

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getFilenamePrefix() {
        return filenamePrefix;
    }

    public void setFilenamePrefix(String filenamePrefix) {
        this.filenamePrefix = filenamePrefix;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileNameSuffix;
    }

    public String getRegexStr() {
        return regexStr;
    }

    public void setRegexStr(String regexStr) {
        this.regexStr = regexStr;
    }

    public String getHeadFilename() {
        return headFilename;
    }

    public void setHeadFilename(String headFilename) {
        this.headFilename = headFilename;
    }

    @Override
    public String toString(){
        return "MonitorJob: " + monitorName + " | CreatedAt: " + createdAt;
    }
}
