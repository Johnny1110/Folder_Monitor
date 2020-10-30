package com.frizo.lib.foldermonitor.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameUtils {

    public static Instant parseInsantFromFileName(String filename, String regexStr, SimpleDateFormat sdf) throws ParseException {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()){
            String dateStr = matcher.group();
            return sdf.parse(dateStr).toInstant();
        }else {
            return null;
        }
    }

    public static boolean isCompressFile(String filename){
        return filename.endsWith(".zip") || filename.endsWith(".Z") || filename.endsWith(".gz");
    }

}
