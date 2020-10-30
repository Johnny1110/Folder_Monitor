package com.frizo.lib.foldermonitor.core;

import java.io.IOException;

public interface Monitor {

    void init();

    void prescan() throws IOException;

    void cleanStorage();

    void complatePreviousLines();

    void startup() throws Exception;

    void quickRestart() throws Exception;

    void restart() throws Exception;

    void close() throws Exception;

}
