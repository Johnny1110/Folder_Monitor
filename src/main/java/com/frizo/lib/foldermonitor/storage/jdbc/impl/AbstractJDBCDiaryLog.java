package com.frizo.lib.foldermonitor.storage.jdbc.impl;

import com.frizo.lib.foldermonitor.storage.jdbc.JDBCDiaryLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

abstract class AbstractJDBCDiaryLog implements JDBCDiaryLog {

        protected Connection conn;

        protected String url;

        protected String username;

        protected String password;

        protected String driverName;

        protected AbstractJDBCDiaryLog(String driverName, String url,String username, String password){
            this.driverName = driverName;
            this.url = url;
            this.username = url;
            this.password = url;
        }

        @Override
        public void init() {
            logger.info("load JDBC driver.");
            loadDriver(driverName);
            try {
                conn = DriverManager.getConnection(url, username, password);
                logger.info("already connected with : " + url);
            } catch (SQLException ex) {
                logger.error("DriverManager failed to connect with: " + url);
            }
        }

        @Override
        public void close() {
            try {
                conn.close();
                logger.info("close connection successfully.");
            } catch (SQLException ex) {
                logger.error("failed to close connection.", ex);
            }
        }

}
