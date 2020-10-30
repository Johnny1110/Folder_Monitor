package com.frizo.lib.foldermonitor.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCodeUtils {

    public static String md5HashCode(Path absPath) throws Exception {
        FileInputStream fis = new FileInputStream(absPath.toFile());
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int length = -1;
        while ((length = fis.read(buffer, 0, 1024)) != -1) {
            md.update(buffer, 0, length);
        }
        fis.close();
        byte[] md5Bytes = md.digest();
        BigInteger bigInt = new BigInteger(1, md5Bytes); // 1代表絕對值
        return bigInt.toString(16);//轉換為16進位制
    }

    public static String md5HashCode(InputStream is) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int length = -1;
        while ((length = is.read(buffer, 0, 1024)) != -1) {
            md.update(buffer, 0, length);
        }
        is.close();
        byte[] md5Bytes = md.digest();
        BigInteger bigInt = new BigInteger(1, md5Bytes); // 1代表絕對值
        return bigInt.toString(16);//轉換為16進位制
    }

}
