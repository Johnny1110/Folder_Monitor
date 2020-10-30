package com.frizo.lib.foldermonitor.impl;

import com.frizo.lib.foldermonitor.core.Monitor;
import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.core.StorageType;
import com.frizo.lib.foldermonitor.exception.MonitorFactoryException;

public class MonitorFactory {

    public static Monitor buildMonitor(MonitorProperty property){
        StorageType storageType = property.getStorageType();
        Monitor monitor = null;
        switch (storageType){
            case XML_STORAGE:
                throw new MonitorFactoryException("XML_STORAGE not support yet.");

            case FILE_STORAGE:
                throw new MonitorFactoryException("FILE_STORAGE not support yet.");

            case JDBC_STORAGE:
                monitor = new LogFileMonitor(property);
                break;

            default:
                throw new MonitorFactoryException("StorageType selected goes wrong.");
        }

        return monitor;
    }

}
