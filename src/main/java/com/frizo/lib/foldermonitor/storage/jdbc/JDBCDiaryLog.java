package com.frizo.lib.foldermonitor.storage.jdbc;

import com.frizo.lib.foldermonitor.storage.DiaryLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface JDBCDiaryLog extends DiaryLog {

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    default void loadDriver(String driverName){
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            logger.error("failed to load jdbc driver.", e);
        }
    }
}
