package com.frizo.lib.foldermonitor.core;

@FunctionalInterface
public interface RecordReader<T> {

    void readRecord(T t);

    default void flush(){
        return;
    }

}
