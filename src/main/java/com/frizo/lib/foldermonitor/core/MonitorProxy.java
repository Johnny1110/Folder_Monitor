package com.frizo.lib.foldermonitor.core;

import com.frizo.lib.foldermonitor.core.Monitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitorProxy implements Monitor {

    private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private Monitor monitor;

    public MonitorProxy(Monitor monitor){
        this.monitor = monitor;
    }


    @Override
    public void init() {
        try {
            logger.info("monitor: initial.");
            monitor.init();
            logger.info("monitor: initial done.");
        }catch (Exception ex){
            logger.error("monitor: failed to init monitor.", ex);
            throw ex;
        }
    }

    @Override
    public void prescan() {
        try {
            logger.info("monitor: start to prescan.");
            monitor.prescan();
            logger.info("monitor: prescan finished.");
        }catch (Exception ex){
            logger.error("monitor: failed to prescan.", ex);
        }
    }

    @Override
    public void cleanStorage() {
        try {
            logger.info("monitor: start to clean Storage.");
            monitor.cleanStorage();
            logger.info("monitor: already cleaned the storage.");
        }catch (Exception ex){
            logger.error("monitor: failed to clean storage.", ex);
        }
    }

    @Override
    public void complatePreviousLines() {
        try {
            logger.info("monitor: start to complate previous lines.");
            monitor.complatePreviousLines();
            logger.info("monitor: already complated previous lines.");
        }catch (Exception ex){
            logger.error("monitor: failed to complate previous lines.", ex);
        }
    }

    @Override
    public void startup() throws Exception {
        try {
            logger.info("monitor: try to startup.");
            monitor.startup();
            logger.info("monitor: startup success.");
        }catch (Exception ex){
            logger.error("monitor: failed to startup.", ex);
            throw ex;
        }
    }

    @Override
    public void quickRestart() throws Exception {
        try {
            logger.info("monitor: try to quick startup.");
            monitor.quickRestart();
            logger.info("monitor: quick startup success.");
        }catch (Exception ex){
            logger.error("monitor: failed to quick restart.", ex);
            throw ex;
        }
    }

    @Override
    public void restart() throws Exception {
        try {
            logger.info("monitor: try to restart.");
            monitor.restart();
            logger.info("monitor: restart success.");
        }catch (Exception ex){
            logger.error("monitor: failed to restart.", ex);
            throw ex;
        }
    }

    @Override
    public void close() throws Exception {
        try {
            logger.info("monitor: try to close");
            monitor.close();
            logger.info("monitor: close success.");
        }catch (Exception ex){
            logger.error("monitor: failed to close.", ex);
            throw ex;
        }
    }
}
