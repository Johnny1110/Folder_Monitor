package com.frizo.lib.foldermonitor.core;

import com.frizo.lib.foldermonitor.storage.DiaryLog;

import java.text.SimpleDateFormat;
import java.time.Instant;

public class MonitorProperty {

    private String headFileName;

    private String fileNamePrefix;

    private String fileNameSuffix;

    private StorageType storageType;

    private boolean readPrevious;

    private String dateRegex;

    private String monitorName;

    private String folderPath;

    private Instant cutOffDate;

    private SimpleDateFormat dateFormate;

    private long interval;

    private RecordReader recordReader;

    private DiaryLog diaryLog;

    public String getHeadFileName() {
        return headFileName;
    }

    public MonitorProperty setHeadFileName(String headFileName) {
        this.headFileName = headFileName;
        return this;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public MonitorProperty setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
        return this;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }

    public MonitorProperty setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileNameSuffix;
        return this;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public MonitorProperty setStorageType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    public boolean isReadPrevious() {
        return readPrevious;
    }

    public MonitorProperty setReadPrevious(boolean readPrevious) {
        this.readPrevious = readPrevious;
        return this;
    }

    public String getDateRegex() {
        return dateRegex;
    }

    public MonitorProperty setDateRegex(String dateRegex) {
        this.dateRegex = dateRegex;
        return this;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public MonitorProperty setMonitorName(String monitorName) {
        this.monitorName = monitorName;
        return this;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public MonitorProperty setFolderPath(String folderPath) {
        this.folderPath = folderPath;
        return this;
    }

    public Instant getCutOffDate() {
        return cutOffDate;
    }

    public MonitorProperty setCutOffDate(Instant cutOffDate) {
        this.cutOffDate = cutOffDate;
        return this;
    }

    public SimpleDateFormat getDateFormate() {
        return dateFormate;
    }

    public MonitorProperty setDateFormate(SimpleDateFormat dateFormate) {
        this.dateFormate = dateFormate;
        return this;
    }

    public long getInterval() {
        return interval;
    }

    public MonitorProperty setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public RecordReader getRecordReader() {
        return recordReader;
    }

    public MonitorProperty setRecordReader(RecordReader recordReader) {
        this.recordReader = recordReader;
        return this;
    }

    public DiaryLog getDiaryLog() {
        return diaryLog;
    }

    public MonitorProperty setDiaryLog(DiaryLog diaryLog) {
        this.diaryLog = diaryLog;
        return this;
    }
}
