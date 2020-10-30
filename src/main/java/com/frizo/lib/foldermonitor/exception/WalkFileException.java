package com.frizo.lib.foldermonitor.exception;

public class WalkFileException extends RuntimeException {

    public WalkFileException(String msg, Throwable e){
        super(msg, e);
    }

    public WalkFileException(String msg){
        super(msg);
    }
}
