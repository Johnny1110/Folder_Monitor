package com.frizo.lib.foldermonitor.listener;

import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.core.RecordReader;
import com.frizo.lib.foldermonitor.io.FileNameUtils;
import com.frizo.lib.foldermonitor.io.HashCodeUtils;
import com.frizo.lib.foldermonitor.io.MonitorIOUtils;
import com.frizo.lib.foldermonitor.storage.DiaryLog;
import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogFileListener extends FileAlterationListenerAdaptor {

    private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private DiaryLog diaryLog;

    private MonitorJob monitorJob;

    private RecordReader<String> reader;

    private MonitorProperty property;

    public LogFileListener(MonitorJob monitorJob, MonitorProperty property, DiaryLog diaryLog, RecordReader<String> reader) {
        this.diaryLog = diaryLog;
        this.monitorJob = monitorJob;
        this.reader = reader;
        this.property = property;
    }

    @Override
    public void onFileChange(File file) {
        logger.info("[Change]:" + file.getAbsolutePath());
        String filename = file.toPath().getFileName().toString(); // 取得 filename
        FolderFile ff = diaryLog.getFolderFileByFilename(filename);
        MonitorIOUtils.compareAndFillRead(monitorJob.getFolderPath(), ff, reader);
        List<FolderFile> ffs = new ArrayList<>();
        ffs.add(ff);
        diaryLog.updateFolderFiles(ffs);
    }

    @Override
    public void onFileCreate(File file) {
        logger.info("[Create]:" + file.getAbsolutePath());
        String filename = file.toPath().getFileName().toString(); // 取得 filename

        BasicFileAttributes fatr = null;
        try {
            fatr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FolderFile headFile = diaryLog.getFolderFileByFilename(monitorJob.getHeadFilename());

        if (FileNameUtils.isCompressFile(filename)) { // 處理壓縮檔
            MonitorIOUtils.updateCompressFile(file, diaryLog);
        } else if (filename.equals(monitorJob.getHeadFilename())) { // 處理新增 head file

            if (headFile != null) {
                headFile.setLogDate(fatr.creationTime().toInstant());
                headFile.setUpdatedAt(fatr.lastModifiedTime().toInstant());
                headFile.setCreatedAt(fatr.creationTime().toInstant());
                diaryLog.resetHeadFile(headFile);

            } else {
                FolderFile folderFile = new FolderFile();
                folderFile.setFilename(filename);
                folderFile.setLogDate(fatr.creationTime().toInstant());
                folderFile.setParentPackName("");
                folderFile.setUpdatedAt(fatr.lastModifiedTime().toInstant());
                folderFile.setCreatedAt(fatr.creationTime().toInstant());
                folderFile.setHashCode("0");
                folderFile.setMonitorName(monitorJob.getMonitorName());
                folderFile.setReadLine(0);
                diaryLog.insertOrIgnoreFolderFile(folderFile);
            }

        } else { // 處理新增帶有時間戳記的檔案
            FolderFile newFile = new FolderFile();
            if (headFile != null) {
                try {
                    newFile.setReadLine(headFile.getReadLine());
                    newFile.setMonitorName(headFile.getMonitorName());
                    newFile.setHashCode(HashCodeUtils.md5HashCode(file.toPath()));
                    newFile.setCreatedAt(headFile.getCreatedAt());
                    newFile.setUpdatedAt(headFile.getUpdatedAt());
                    newFile.setParentPackName("");
                    Instant logdate = FileNameUtils.parseInsantFromFileName(filename, monitorJob.getRegexStr(), property.getDateFormate());
                    newFile.setLogDate(logdate);
                    newFile.setFilename(filename);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    newFile.setReadLine(0);
                    newFile.setMonitorName(monitorJob.getMonitorName());
                    newFile.setHashCode("0");
                    newFile.setCreatedAt(fatr.creationTime().toInstant());
                    newFile.setUpdatedAt(fatr.lastModifiedTime().toInstant());
                    newFile.setParentPackName("");
                    Instant logdate = FileNameUtils.parseInsantFromFileName(filename, monitorJob.getRegexStr(), property.getDateFormate());
                    newFile.setLogDate(logdate);
                    newFile.setFilename(filename);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            System.out.println("new file::" + newFile);
            diaryLog.insertOrIgnoreFolderFile(newFile);
        }

    }


    @Override
    public void onFileDelete(File file) {
        logger.info("[Delete]:" + file.getAbsolutePath());
        String filename = file.toPath().getFileName().toString(); // 取得 filename
        if (filename.equals(monitorJob.getHeadFilename())){
            diaryLog.deleteFolderFileByFilename(filename);
        }if (FileNameUtils.isCompressFile(filename)){
            Set<String> packnames = new HashSet<>();
            packnames.add(filename);
            diaryLog.deleteFolderFilesByParentPackNames(packnames);
        }
    }



    @Override
    public void onStart(FileAlterationObserver observer) {
        // TODO Auto-generated method stub super.onStart(observer);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        // TODO Auto-generated method stub super.onStop(observer);
    }

}
