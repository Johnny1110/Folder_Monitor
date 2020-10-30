package com.frizo.lib.foldermonitor;

import com.frizo.lib.foldermonitor.core.Monitor;
import com.frizo.lib.foldermonitor.core.MonitorProperty;
import com.frizo.lib.foldermonitor.core.MonitorProxy;
import com.frizo.lib.foldermonitor.core.StorageType;
import com.frizo.lib.foldermonitor.impl.MonitorFactory;
import com.frizo.lib.foldermonitor.storage.jdbc.impl.SqliteDiaryLog;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) throws Exception {
        MonitorProperty property = new MonitorProperty();
        property.setMonitorName("Monitor_01")
                .setFolderPath("D:\\logs")
                .setCutOffDate(LocalDate.of(2020, 10, 29).atStartOfDay(ZoneId.systemDefault()).toInstant())
                .setDateFormate(new SimpleDateFormat("yyyy-MM-dd"))
                .setDateRegex("[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]")
                .setHeadFileName("schedule.log")
                .setFileNamePrefix("schedule")
                .setFileNameSuffix(".log")
                .setReadPrevious(true)
                .setInterval(1000L)
                .setStorageType(StorageType.JDBC_STORAGE)
                .setRecordReader(System.out::println)
                .setDiaryLog(new SqliteDiaryLog("jdbc:sqlite:D:/workspace/lib/test.db", null, null));


        Monitor monitor = new MonitorProxy(MonitorFactory.buildMonitor(property));


        //-- 啟動操作 --//

        monitor.init();
        monitor.cleanStorage();
        monitor.prescan();
        monitor.complatePreviousLines();
        monitor.startup();

        Thread.sleep(1000L);

        monitor.quickRestart();
    }



}
