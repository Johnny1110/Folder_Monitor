package com.frizo.lib.foldermonitor.io;

import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.exception.WalkFileException;
import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MonitorFileWalker extends SimpleFileVisitor<Path> {

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private List<FolderFile> qualifiedFiles = new ArrayList<>();

    private MonitorJob monitorJob;

    private MonitorProperty property;

    private Pattern pattern;

    public MonitorFileWalker(MonitorJob monitorJob, MonitorProperty property){
        this.monitorJob = monitorJob;
        this.property = property;
        this.pattern = Pattern.compile(property.getDateRegex());
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {

        String fileName = path.getFileName().toString();

        try {

            if (fileName.endsWith(".zip")) {
                Optional<List<FolderFile>> files = processZipFile(monitorJob, property, path, attributes);
                files.ifPresent(qualifiedFiles::addAll);
            } else if (fileName.endsWith(".Z")) {
                Optional<List<FolderFile>> files = processZFile(monitorJob, property, path, attributes);
                files.ifPresent(qualifiedFiles::addAll);
            } else if (fileName.endsWith(".gz")) {
                Optional<List<FolderFile>> files = processGzFile(monitorJob, property, path, attributes);
                files.ifPresent(qualifiedFiles::addAll);
            } else {
                Optional<FolderFile> file = processNormalFile(monitorJob, property, path, attributes);
                file.ifPresent(qualifiedFiles::add);
            }

        }catch (Exception ex){
            logger.error("error occor when walking file tree", ex);
            throw new WalkFileException("error occor when walking file tree", ex);
        }

        return FileVisitResult.CONTINUE;
    }



    private Optional<List<FolderFile>> processZipFile(MonitorJob monitorJob, MonitorProperty property, Path path, BasicFileAttributes attributes) throws Exception {
        List<FolderFile> files = new ArrayList<>();
        logger.info("parse zip file: " + path);
        File zipFile = path.toFile();
        ZipFile zf = new ZipFile(path.toFile());
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (!ze.isDirectory()) {
                String filename = ze.getName();
                if (filename.startsWith(monitorJob.getFilenamePrefix()) && filename.endsWith(monitorJob.getFileNameSuffix())) {
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        String dateString = matcher.group();
                        Instant date = property.getDateFormate().parse(dateString).toInstant();
                        date = date.atZone(ZoneId.systemDefault()).toInstant();
                        if (date.isAfter(property.getCutOffDate())) {
                            System.out.println("zip> time: " + date + "，filename: " + filename);
                            FolderFile file = new FolderFile();
                            file.setFilename(filename);
                            file.setCreatedAt(attributes.creationTime().toInstant());
                            file.setUpdatedAt(attributes.lastModifiedTime().toInstant());
                            file.setMonitorName(monitorJob.getMonitorName());
                            file.setLogDate(date);
                            file.setParentPackName(path.getFileName().toString());
                            file.setHashCode("0");
                            files.add(file);
                        }
                    }
                }
            }
        }
        zin.close();
        zf.close();
        return Optional.ofNullable(files);
    }

    private Optional<List<FolderFile>> processZFile(MonitorJob monitorJob, MonitorProperty property, Path path, BasicFileAttributes attributes) throws Exception {
        return null;
    }

    private Optional<List<FolderFile>> processGzFile(MonitorJob monitorJob, MonitorProperty property, Path path, BasicFileAttributes attributes) {
        return null;
    }

    private Optional<FolderFile> processNormalFile(MonitorJob monitorJob, MonitorProperty property, Path path, BasicFileAttributes attributes) throws Exception {
        FolderFile file = null;

        String filename = path.getFileName().toString();
        if (filename.startsWith(monitorJob.getFilenamePrefix()) && filename.endsWith(monitorJob.getFileNameSuffix())) {
            Matcher matcher = pattern.matcher(filename);

            Instant date;
            if (matcher.find()) {
                String dateString = matcher.group();
                date = property.getDateFormate().parse(dateString).toInstant();
                date = date.atZone(ZoneId.systemDefault()).toInstant();
            }else{
                date = attributes.creationTime().toInstant();
                date = date.atZone(ZoneId.systemDefault()).toInstant();
            }

            if (date.isAfter(property.getCutOffDate())) {
                System.out.println("一般> 合格的時間: " + date + "，合格的檔案: " + filename);
                file = new FolderFile();
                file.setMonitorName(monitorJob.getMonitorName());
                file.setLogDate(date);
                file.setCreatedAt(attributes.creationTime().toInstant());
                file.setUpdatedAt(attributes.lastModifiedTime().toInstant());
                file.setFilename(filename);
                file.setParentPackName("");
                file.setHashCode("0");
            }
        }
        return Optional.ofNullable(file);
    }

    public List<FolderFile> getQualifiedFiles(){
        Collections.sort(qualifiedFiles);
        return this.qualifiedFiles;
    }

}
