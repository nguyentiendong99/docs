/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bkav.lk.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MD5Util {

    private final static Logger log = LoggerFactory.getLogger(MD5Util.class);

    public static String MD5(String fileUrl){
        FileInputStream fis = null;
        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fis = new FileInputStream(fileUrl);

            byte[] dataBytes = new byte[1024];

            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            log.error("get MD5 error", ex);
            return "";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    log.error("get MD5 error: ", ex);
                }
            }
        }
    }

    public static String MD5(byte[] file) {
        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            byte[] mdbytes = md.digest(file);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            log.error("get MD5 error: ", ex);
            return "";
        }
    }
}
