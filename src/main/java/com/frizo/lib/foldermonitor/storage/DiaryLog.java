package com.frizo.lib.foldermonitor.storage;


import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface DiaryLog {

    void init();

    void close();

    void createMonitorJobIfNotExist(MonitorJob monitorJob);

    MonitorJob getMonitorJobByName(String monitorName);

    void insertOrIgnoreFolderFiles(List<FolderFile> fileList);

    List<FolderFile> findAllfileByMonitorName(String monitorName);

    void deleteFolderFiles(List<FolderFile> files);

    void deleteFolderFilesByParentPackNames(Set<String> packName);

    void resetHeadFile(FolderFile headfile);

    FolderFile getFolderFileByFilename(String filename);

    List<FolderFile> findAllFileByMonitorNameAndAfterInstant(String monitorName, Instant cutOffDate);

    void updateFolderFiles(List<FolderFile> files);

    void insertOrIgnoreFolderFile(FolderFile file);

    void deleteFolderFileByFilename(String filename);
}
