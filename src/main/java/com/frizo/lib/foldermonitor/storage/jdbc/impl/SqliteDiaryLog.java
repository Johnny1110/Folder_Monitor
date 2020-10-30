package com.frizo.lib.foldermonitor.storage.jdbc.impl;


import com.frizo.lib.foldermonitor.storage.model.FolderFile;
import com.frizo.lib.foldermonitor.storage.model.MonitorJob;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SqliteDiaryLog extends AbstractJDBCDiaryLog {

    private static final String DRIVER_NAME = "org.sqlite.JDBC";

    public SqliteDiaryLog(String url, String username, String password) {
        super(DRIVER_NAME, url, username, password);
    }

    @Override
    public void createMonitorJobIfNotExist(MonitorJob monitorJob) {
        String sql = "INSERT OR IGNORE INTO monitor_jobs(folder_path, created_at, filename_prefix, filename_suffix, regex_str, monitor_name, head_filename) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, monitorJob.getFolderPath());
            pstat.setTimestamp(2, Timestamp.from(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toInstant(ZoneOffset.UTC)));
            pstat.setString(3, monitorJob.getFilenamePrefix());
            pstat.setString(4, monitorJob.getFileNameSuffix());
            pstat.setString(5, monitorJob.getRegexStr());
            pstat.setString(6, monitorJob.getMonitorName()); // PK
            pstat.setString(7, monitorJob.getHeadFilename());
            pstat.execute();
        } catch (SQLException ex) {
            logger.info("failed to create monitor job", ex);
        }
    }

    @Override
    public MonitorJob getMonitorJobByName(String monitorName) {
        MonitorJob job = null;
        String sql = "SELECT * FROM monitor_jobs WHERE monitor_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, monitorName);
            ResultSet rs = pstat.executeQuery();
            job = new MonitorJob();
            job.setMonitorName(rs.getString("monitor_name"));
            job.setFolderPath(rs.getString("folder_path"));
            job.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            job.setFilenamePrefix(rs.getString("filename_prefix"));
            job.setFileNameSuffix(rs.getString("filename_suffix"));
            job.setRegexStr(rs.getString("regex_str"));
            job.setHeadFilename(rs.getString("head_filename"));
            rs.close();
        } catch (SQLException ex) {
            logger.info("failed to get monitor job");
        }
        return job;
    }

    @Override
    public void insertOrIgnoreFolderFiles(List<FolderFile> files) {
        String sql = "INSERT OR IGNORE INTO folder_files(file_name, created_at, updated_at, monitor_name, parent_pack_name, log_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            files.forEach(file -> {
                try {
                    pstat.setString(1, file.getFilename());
                    pstat.setTimestamp(2, Timestamp.from(file.getCreatedAt()));
                    pstat.setTimestamp(3, Timestamp.from(file.getUpdatedAt()));
                    pstat.setString(4, file.getMonitorName());
                    pstat.setString(5, file.getParentPackName());
                    pstat.setTimestamp(6, Timestamp.from(file.getLogDate()));
                    pstat.addBatch();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            pstat.executeBatch();
        } catch (SQLException ex) {
            logger.info("failed to create folder files");
        }
    }

    @Override
    public List<FolderFile> findAllfileByMonitorName(String monitorName) {
        List<FolderFile> files = new ArrayList<>();
        String sql = "SELECT * FROM folder_files WHERE monitor_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, monitorName);
            ResultSet rs = pstat.executeQuery();
            while (rs.next()) {
                FolderFile file = new FolderFile();
                file.setFilename(rs.getString("file_name"));
                file.setReadLine(rs.getInt("read_line"));
                file.setHashCode(rs.getString("hash_code"));
                file.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                file.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                file.setMonitorName(rs.getString("monitor_name"));
                file.setParentPackName(rs.getString("parent_pack_name"));
                file.setLogDate(rs.getTimestamp("log_date").toInstant());
                files.add(file);
            }
            rs.close();
        } catch (SQLException ex) {
            logger.info("failed to find all file");
        }
        return files;
    }

    @Override
    public void deleteFolderFiles(List<FolderFile> files) {
        String sql = "DELETE FROM folder_files WHERE file_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {

            files.forEach(f -> {
                try {
                    pstat.setString(1, f.getFilename());
                    pstat.addBatch();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            pstat.executeBatch();
        } catch (SQLException ex) {
            logger.info("failed to delete file by pack name");
        }
    }

    @Override
    public void deleteFolderFilesByParentPackNames(Set<String> packNames) {
        String sql = "DELETE FROM folder_files WHERE parent_pack_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            packNames.forEach(name -> {
                try {
                    pstat.setString(1, name);
                    pstat.addBatch();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            pstat.executeBatch();
        } catch (SQLException ex) {
            logger.info("failed to delete file by pack name");
        }
    }

    @Override
    public void resetHeadFile(FolderFile headfile) {
        String sql = "UPDATE OR IGNORE folder_files SET read_line=?, hash_code=?,created_at=?, updated_at=?, log_date=? WHERE file_name=?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setInt(1, 0);
            pstat.setString(2, "0");
            pstat.setTimestamp(3, Timestamp.from(headfile.getCreatedAt()));
            pstat.setTimestamp(4, Timestamp.from(headfile.getUpdatedAt()));
            pstat.setTimestamp(5, Timestamp.from(headfile.getLogDate()));
            pstat.setString(6, headfile.getFilename());
            pstat.executeUpdate();
        } catch (SQLException ex) {
            logger.info("failed to delete file by pack name");
        }
    }

    @Override
    public FolderFile getFolderFileByFilename(String filename) {
        String sql = "SELECT * FROM folder_files WHERE file_name=?";
        FolderFile file = null;
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, filename);
            ResultSet rs = pstat.executeQuery();
            if (rs.next()) {
                file = new FolderFile();
                file.setFilename(rs.getString("file_name"));
                file.setReadLine(rs.getInt("read_line"));
                file.setHashCode(rs.getString("hash_code"));
                file.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                file.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                file.setMonitorName(rs.getString("monitor_name"));
                file.setParentPackName(rs.getString("parent_pack_name"));
                file.setLogDate(rs.getTimestamp("log_date").toInstant());
            }
            rs.close();
        } catch (SQLException ex) {
            logger.info("failed to find FolderFile by filename.");
        }
        return file;
    }

    @Override
    public List<FolderFile> findAllFileByMonitorNameAndAfterInstant(String monitorName, Instant cutOffDate) {
        List<FolderFile> files = new ArrayList<>();
        String sql = "SELECT * FROM folder_files WHERE monitor_name = ? AND log_date > ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, monitorName);
            pstat.setTimestamp(2, Timestamp.from(cutOffDate));
            ResultSet rs = pstat.executeQuery();
            while (rs.next()) {
                FolderFile file = new FolderFile();
                file.setFilename(rs.getString("file_name"));
                file.setReadLine(rs.getInt("read_line"));
                file.setHashCode(rs.getString("hash_code"));
                file.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                file.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                file.setMonitorName(rs.getString("monitor_name"));
                file.setParentPackName(rs.getString("parent_pack_name"));
                file.setLogDate(rs.getTimestamp("log_date").toInstant());
                files.add(file);
            }
        } catch (SQLException ex) {
            logger.info("failed to find all file.", ex);
        }
        return files;
    }

    @Override
    public void updateFolderFiles(List<FolderFile> files) {
        String sql = "UPDATE folder_files SET read_line = ? , hash_code = ?, updated_at = ?, parent_pack_name = ? WHERE file_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            files.forEach(file -> {
                try {
                    pstat.setInt(1, file.getReadLine());
                    pstat.setString(2, file.getHashCode());
                    pstat.setTimestamp(3, Timestamp.from(file.getUpdatedAt()));
                    pstat.setString(4, file.getParentPackName());
                    pstat.setString(5, file.getFilename());
                    pstat.addBatch();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            pstat.executeBatch();
        } catch (SQLException ex) {
            logger.info("failed to update folder files");
        }
    }

    @Override
    public void insertOrIgnoreFolderFile(FolderFile file) {
        String sql = "INSERT OR IGNORE INTO folder_files(file_name, read_line, hash_code, created_at, updated_at, monitor_name, parent_pack_name, log_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, file.getFilename());
            pstat.setInt(2, file.getReadLine());
            pstat.setString(3, file.getHashCode());
            pstat.setTimestamp(4, Timestamp.from(file.getCreatedAt()));
            pstat.setTimestamp(5, Timestamp.from(file.getUpdatedAt()));
            pstat.setString(6, file.getMonitorName());
            pstat.setString(7, file.getParentPackName());
            pstat.setTimestamp(8, Timestamp.from(file.getLogDate()));
            pstat.execute();
        } catch (SQLException ex) {
            logger.info("failed to insert folder file");
        }
    }

    @Override
    public void deleteFolderFileByFilename(String filename) {
        String sql = "DELETE FROM folder_files WHERE file_name = ?";
        try (PreparedStatement pstat = conn.prepareStatement(sql)) {
            pstat.setString(1, filename);
            pstat.execute();
        } catch (SQLException ex) {
            logger.info("failed to delete file by pack name");
        }
    }
}
