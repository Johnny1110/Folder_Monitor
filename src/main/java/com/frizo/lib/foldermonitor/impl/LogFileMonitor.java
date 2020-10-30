package com.frizo.lib.foldermonitor.impl;

import com.frizo.lib.foldermonitor.core.Monitor;
import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.exception.MonitorInitializeException;
import com.frizo.lib.foldermonitor.io.MonitorIOUtils;
import com.frizo.lib.foldermonitor.listener.LogFileListener;
import com.frizo.lib.foldermonitor.storage.DiaryLog;
import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LogFileMonitor implements Monitor {

    private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private DiaryLog diaryLog;

    private MonitorProperty monitorProperty;

    private MonitorJob monitorJob;

    private FileAlterationMonitor monitor;


    LogFileMonitor(MonitorProperty monitorProperty) {
        this.diaryLog = monitorProperty.getDiaryLog();
        this.monitorProperty = monitorProperty;
    }

    @Override
    public void init() {
        Path folder = Paths.get(monitorProperty.getFolderPath());
        if (!Files.isDirectory(folder) || Files.notExists(folder)) {
            throw new MonitorInitializeException("failed to init monitor, because [folder path] not exist.");
        }

        this.diaryLog.init();
        MonitorJob job = new MonitorJob();
        job.setMonitorName(monitorProperty.getMonitorName());
        job.setFolderPath(monitorProperty.getFolderPath());
        job.setCreatedAt(Instant.now());
        job.setFilenamePrefix(monitorProperty.getFileNamePrefix());
        job.setFileNameSuffix(monitorProperty.getFileNameSuffix());
        job.setRegexStr(monitorProperty.getDateRegex());
        job.setHeadFilename(monitorProperty.getHeadFileName());
        diaryLog.createMonitorJobIfNotExist(job);
        this.monitorJob = diaryLog.getMonitorJobByName(monitorProperty.getMonitorName());
        logger.info("get MonitorJob from DB: " + job);

        if (this.monitor == null) {
            FileAlterationObserver observer = initFileAlterationObserver();
            monitor = new FileAlterationMonitor(monitorProperty.getInterval(), observer);
        }
    }

    private FileAlterationObserver initFileAlterationObserver() {
        IOFileFilter filefilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(monitorJob.getFileNameSuffix()),
                FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
        );

        IOFileFilter zipFilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".zip"),
                FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
        );

        IOFileFilter zFilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".Z"),
                FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
        );

        IOFileFilter gzFilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".gz"),
                FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
        );

        IOFileFilter finalFilter = FileFilterUtils.or(filefilters, zFilters, zipFilters, gzFilters);

        FileAlterationObserver observer = new FileAlterationObserver(monitorJob.getFolderPath(), finalFilter);
        observer.addListener(new LogFileListener(monitorJob, monitorProperty, diaryLog, monitorProperty.getRecordReader()));
        return observer;
    }

    @Override
    public void prescan() throws IOException {
        List<FolderFile> qualifiedFile = MonitorIOUtils.scanAndReturnAllQualifiedFile(monitorJob, monitorProperty);
        diaryLog.insertOrIgnoreFolderFiles(qualifiedFile);
    }

    @Override
    public void cleanStorage() {
        List<FolderFile> files = diaryLog.findAllfileByMonitorName(monitorJob.getMonitorName());
        List<FolderFile> needRemoveFiles = files.stream()
                .filter(f -> {
                    return f.getParentPackName().equals("");
                })
                .filter(f -> {
                    Path path = Paths.get(monitorJob.getFolderPath(), f.getFilename());
                    return Files.notExists(path);
                })
                .collect(Collectors.toList());

        Set<String> needRemovePack = files.stream()
                .filter(f -> {
                    return !f.getParentPackName().equals("");
                })
                .filter(f -> {
                    Path path = Paths.get(monitorJob.getFolderPath(), f.getParentPackName());
                    return Files.notExists(path);
                })
                .map(FolderFile::getParentPackName)
                .collect(Collectors.toSet());

        diaryLog.deleteFolderFiles(needRemoveFiles);
        diaryLog.deleteFolderFilesByParentPackNames(needRemovePack);
    }

    @Override
    public void complatePreviousLines() {
        if (!monitorProperty.isReadPrevious()) {
            return;
        }
        // 先 reset head file
        FolderFile headfile = diaryLog.getFolderFileByFilename(monitorJob.getHeadFilename());
        if (headfile != null) {
            diaryLog.resetHeadFile(headfile);
        }

        List<FolderFile> filesAfterCutOffDate = diaryLog.findAllFileByMonitorNameAndAfterInstant(monitorJob.getMonitorName(), monitorProperty.getCutOffDate());
        Collections.sort(filesAfterCutOffDate); // 按照時間排序好的 files。

        filesAfterCutOffDate.forEach(file -> {
            System.out.println("處理文件: " + file);
            System.out.println("folderPath: " + monitorJob.getFolderPath());
            MonitorIOUtils.compareAndFillRead(monitorJob.getFolderPath(), file, monitorProperty.getRecordReader());
        });
        diaryLog.updateFolderFiles(filesAfterCutOffDate);
    }

    @Override
    public void startup() throws Exception {
        monitor.start();
    }


    @Override
    public void quickRestart() throws Exception {
        close();
        init();
        startup();
    }

    @Override
    public void restart() throws Exception {
        close();
        init();
        prescan();
        cleanStorage();
        complatePreviousLines();
        startup();
    }

    @Override
    public void close() throws Exception {
        this.monitor.stop();
        this.diaryLog.close();

    }
}
