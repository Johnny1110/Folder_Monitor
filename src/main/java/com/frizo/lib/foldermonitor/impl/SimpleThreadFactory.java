package com.frizo.lib.foldermonitor.impl;

import org.apache.commons.io.monitor.FileAlterationMonitor;

import java.util.concurrent.ThreadFactory;

public class SimpleThreadFactory implements ThreadFactory {

    private Thread monitorThread;

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        if(r instanceof FileAlterationMonitor) {
            monitorThread = thread;
        }
        return thread;
    }

    public void killThread(){
        monitorThread.stop();
    }

    public boolean isMonitorThreadAlive() {
        boolean isAlive = false;
        if(monitorThread != null) {
            isAlive = monitorThread.isAlive();
        }
        return isAlive;
    }
}
