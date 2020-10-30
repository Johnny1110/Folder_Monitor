package com.frizo.lib.foldermonitor.io;

import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.core.RecordReader;
import com.frizo.lib.foldermonitor.storage.DiaryLog;
import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MonitorIOUtils {

    private static Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    public static List<FolderFile> scanAndReturnAllQualifiedFile(MonitorJob monitorJob, MonitorProperty monitorProperty) throws IOException {
        Path path = Paths.get(monitorJob.getFolderPath());
        MonitorFileWalker fileWalker = new MonitorFileWalker(monitorJob, monitorProperty);
        Files.walkFileTree(path, fileWalker);
        return fileWalker.getQualifiedFiles();
    }

    public static void compareAndFillRead(String folderPath, FolderFile file, RecordReader reader) {
        // 一般單檔案
        if (file.getParentPackName().equals("")) {
            readNormalFile(folderPath, file, reader);
        } else {
            readCompressedFile(folderPath, file, reader);
        }
    }

    private static void readNormalFile(String folderPath, FolderFile file, RecordReader reader) {
        Path fpath = Paths.get(folderPath, file.getFilename());
        try {
            String hashcode = HashCodeUtils.md5HashCode(fpath);
            if (!hashcode.equals(file.getHashCode())) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fpath.toFile())));
                if (file.getReadLine() < 0 || file.getReadLine() > getTotalLines(fpath)) {
                    br.close();
                    logger.error("file read-line with wrong value.");
                    return;
                }
                int lineNum = 0;
                String line = br.readLine();
                while (line != null) {
                    if (lineNum < file.getReadLine()) { // 還沒到指定行數，跳下一筆。
                        line = br.readLine();
                        lineNum++;
                        continue;
                    }
                    reader.readRecord(line);
                    file.setReadLine(++lineNum);
                    line = br.readLine();
                }
                // 讀完檔處理一下 worklog
                file.setUpdatedAt(Instant.now());
                file.setHashCode(hashcode);
                br.close();
            }
        }catch (Exception ex){
            logger.error("can not find log file: " + file.getFilename(), ex);
        }
    }



    private static void readCompressedFile(String folderPath, FolderFile file, RecordReader reader) {
        Path cfpath = Paths.get(folderPath, file.getParentPackName());
        if (file.getParentPackName().endsWith(".zip")) {
            readZipFile(cfpath, file, reader);
        }
        if (file.getParentPackName().endsWith(".gz")) {
            readGzFile(cfpath, file, reader);
        }

        if (file.getParentPackName().endsWith(".Z")) {
            readZFile(cfpath, file, reader);
        }
    }


    private static int getTotalLines(Path absPath) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(absPath.toFile())));
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

    static int getTotalLines(Path zipPath, FolderFile file) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath.toFile());
        InputStream zipfileInputStream = zipFile.getInputStream(zipFile.getEntry(file.getFilename()));
        BufferedReader in = new BufferedReader(new InputStreamReader(zipfileInputStream));
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }


    private static void readZFile(Path zpath, FolderFile file, RecordReader reader) {

    }

    private static void readGzFile(Path gzPath, FolderFile file, RecordReader reader) {

    }

    private static void readZipFile(Path zipPath, FolderFile file, RecordReader reader) {
        try {
            System.out.println("讀取 zip 中的 file: " + file.getFilename());
            ZipFile zipFile = new ZipFile(zipPath.toFile());
            InputStream zipfileInputStream = zipFile.getInputStream(zipFile.getEntry(file.getFilename()));
            String hashcode = HashCodeUtils.md5HashCode(zipFile.getInputStream(zipFile.getEntry(file.getFilename())));
            if (!hashcode.equals(file.getHashCode())) {
                BufferedReader br = new BufferedReader(new InputStreamReader(zipfileInputStream));
                if (file.getReadLine() < 0 || file.getReadLine() > getTotalLines(zipPath, file)) {
                    br.close();
                    logger.error("file read-line with wrong value.");
                    return;
                }

                int lineNum = 0;
                String line = br.readLine();
                while (line != null) {
                    if (lineNum < file.getReadLine()) { // 還沒到指定行數，跳下一筆。
                        line = br.readLine();
                        lineNum++;
                        continue;
                    }
                    reader.readRecord(line);
                    file.setReadLine(++lineNum);
                    line = br.readLine();
                }
                // 讀完檔處理一下 worklog
                file.setUpdatedAt(Instant.now());
                file.setHashCode(hashcode);
                br.close();
            }
        } catch (Exception e) {
            logger.warn("some error occur when reading zip file.");
        }
    }


    public static void updateCompressFile(File file, DiaryLog diaryLog){
        String filename = file.toPath().getFileName().toString(); // 取得 filename
        if (filename.endsWith(".zip")){
            updateZipFiles(file, diaryLog);
        }
        if (filename.endsWith(".Z")){
            updateZFiles(file, diaryLog);
        }
        if (filename.endsWith(".gz")){
            updateGzFiles(file, diaryLog);
        }
    }

    private static void updateGzFiles(File gzfile, DiaryLog diaryLog) {
        //TO-DO
    }

    private static void updateZFiles(File zfile, DiaryLog diaryLog) {
        //TO-DO
    }

    private static void updateZipFiles(File zipFile, DiaryLog diaryLog) {
        List<FolderFile> files = new ArrayList<>();
        try {
            ZipFile zf = new ZipFile(zipFile);
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (!ze.isDirectory()) {
                    String parentPackName = zipFile.getName();
                    String fileName = ze.getName();
                    FolderFile file = diaryLog.getFolderFileByFilename(fileName);
                    if (file != null){
                        file.setParentPackName(parentPackName);
                        files.add(file);
                    }
                }
            }
            zin.close();
            zf.close();
            diaryLog.updateFolderFiles(files);
        } catch (IOException e) {
            logger.error("error occur when update zip files.", e);
        }

    }
}
