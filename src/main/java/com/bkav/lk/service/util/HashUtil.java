package com.bkav.lk.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.UUID;

/**
 * @author sangnv7
 */
public class HashUtil {

    private final static Logger log = LoggerFactory.getLogger(HashUtil.class);

    public static String SHA256(byte[] file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(file);
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("get SHA256 error: ", ex);
            return "";
        }
    }

    public static String SHA256(String fileUrl) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            fis = new FileInputStream(fileUrl);
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            log.error("get SHA256 error", ex);
            return "";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    log.error("get SHA256 error: ", ex);
                }
            }
        }
    }

    public static String SHA256Random() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] mdbytes = digest.digest(UUID.randomUUID().toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("get SHA256 error", e);
            return "";
        }
    }

    public static String HmacSHA256(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(rawHmac);
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        try(Formatter formatter = new Formatter(sb)) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return sb.toString();
        }
    }
}
