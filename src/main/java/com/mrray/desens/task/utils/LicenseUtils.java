package com.mrray.desens.task.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.List;

public abstract class LicenseUtils {

    private static final Logger logger = LoggerFactory.getLogger(LicenseUtils.class);

    /**
     * RSA 私钥
     */
    private static final String PRI_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwhoiI3b6F7RtA2A7Dv5yrHPJ84XnTlcopg5B2wbeYsqtxZzWi+Mk4cx1dAV11UXOIx023dMGBNswFDXpnNTCBorKDkF9rVoATLqA9jYArA1H0R8j6Ga5P/cGeomN87FT+tNbwEtif+FJSmY5K5+TjY7kFeQczy4SRq2g2KPMRDKBuNnxlcr/J3Y838g5YCK4uC7WrrZHOAunA9BT4f786tMzwNOc57XeGBRTVeI2DdnfcHNVaX2RsyxwWf8mavVA6Bw64rak2blhgophVdkYf7xTZHU8o9inlN5aNzLSDpqOvUAUluIqv0YSlQepnYMtbZtxJInqO3OTOiC3dYSuKQIDAQAB";

    /**
     * License 存放的文件名
     */
    public static final String LICENSE = "License";

    public static void main(String[] args) {
        String serial = "38aa14e28d840412729a217de01c6ec6";
        String licenseStr = "UHIykGd14WUOG1gN2YwNjQvEYTGzNbG4ZtQlPUBC2JuJWz3C+Ivy33P/Acb0xZ1ltbLCD2uuxZmuPZX8HtMzO7p3GrBx8/5JCQ20OBrszNBL8gaSlU8XW8o1fMPYASITSky8qepQSqAOKpIYG1MdJvOc2Q8WDoI2gnzsGR+5KyC5F1ZtFa2Ts8rg4d3m7o6zn2tWI1Xg5L5RBzD6S8lvg2V6ybfBKrp6yZf4F8PqJIpudny7avEEIz4I1BchTDU7qya/NQ2E8v0WBCMx6SP06x7ZQkv/QpzznUaEG8OrkMms6jDWf1TTU+wru2pdYO+Pmmdmo9SVbgqihPyETNuZD4T+2T9tVGFa8LHlktD4aQ5DPq/XFln8oG+jZWBe1deT0zVT/25CjDWM7j9135GH0vmgOq8Wg778gpiRsU+30dV+OcNHOesRMl7bpuNqgmrZp63/2lFkL7SwwnIUS34d7QA0QhIe2Lku7f5vJtV06Qs=";

        System.out.println(verifyLicense(serial, licenseStr));
        System.out.println(expireTime(serial, licenseStr));
        System.out.println(isExpire(serial, licenseStr));

    }

    /**
     * 校验License
     * @param serial
     * @param license
     * @return
     */
    public static boolean verifyLicense(String serial, String license) {

        logger.info("serial: {}", serial);
        logger.info("license: {}", license);

        try {
            String body = aesDecrypt(serial, license);

            if (StringUtils.hasText(body)) {
                String payload = extrPayload(serial, body);
                String sign = extrSign(body);
                if (StringUtils.hasText(payload) && StringUtils.hasText(sign)) {
                    return verifySign(payload, sign);
                }
            }
        } catch (NoSuchPaddingException | UnsupportedEncodingException |
                NoSuchAlgorithmException | InvalidKeyException |
                InvalidAlgorithmParameterException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeySpecException | SignatureException e) {

            logger.error(e.getMessage(), e);
        }

        return false;
    }

    public static String expireTime(String serial, String license) {


        try {
            if (verifyLicense(serial, license)) {
                String body = aesDecrypt(serial, license);
                if (StringUtils.hasText(body)) {
                    String expire = extrExpire(body);
                    if (StringUtils.hasText(expire)) {
                        return DateFormatUtils.format(Long.parseLong(expire) * 1000, "yyyy-MM-dd HH:mm:ss");
                    }
                }
            }

        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                UnsupportedEncodingException | InvalidAlgorithmParameterException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {

            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static boolean isExpire(String serial, String license) {

        try {
            if (verifyLicense(serial, license)) {
                String body = aesDecrypt(serial, license);
                if (StringUtils.hasText(body)) {
                    String expire = extrExpire(body);
                    if (StringUtils.hasText(expire)) {
                        Date expireDate = new Date(Long.parseLong(expire) * 1000);
                        Date now = new Date();
                        return now.after(expireDate);
                    }
                }
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                UnsupportedEncodingException | InvalidAlgorithmParameterException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {

            logger.error(e.getMessage(), e);
        }

        return true;
    }

    /**
     * 是否已经激活
     *
     * @return
     */
    public static boolean isActive() {
        try {
            File licenseFile = new File("./", LICENSE);
            if (licenseFile.exists()) {
                List<String> lines = FileUtils.readLines(licenseFile, "UTF-8");// 获取序列号
                if (lines != null && lines.size() > 0) {
                    String license = lines.get(0);
                    String serial = serial();
                    if (StringUtils.hasText(license) && StringUtils.hasText(serial)) {
                        return verifyLicense(serial, license);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * AES 解密
     * AES/CBC/NoPadding -- OK
     * AES/CBC/PKCS5Padding
     * AES/ECB/NoPadding
     * AES/ECB/PKCS5Padding
     *
     * example:
     *  https://stackoverflow.com/questions/15554296/simple-java-aes-encrypt-decrypt-example
     *  https://stackoverflow.com/questions/29607500/padding-exception-given-final-block-not-properly-padded
     * @param key
     * @param data
     * @return
     */
    private static String aesDecrypt(String key, String data) throws NoSuchPaddingException,
            NoSuchAlgorithmException, UnsupportedEncodingException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        IvParameterSpec iv = new IvParameterSpec("0000000000000000".getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] original = cipher.doFinal(Base64Utils.decodeFromString(data));

        return new String(original);
    }

    /**
     * RSA 加密
     * @param data
     * @return
     *
     * https://stackoverflow.com/questions/11410770/load-rsa-public-key-from-file/19387517
     * https://stackoverflow.com/questions/35922727/java-security-nosuchalgorithmexception-rsa-signature-not-available
     */
    private static boolean verifySign(String data, String sign) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            UnsupportedEncodingException, SignatureException {

//            byte[] keyBytes = Files.readAllBytes(Paths.get("public_key.der"));
//            System.out.println(Base64Utils.encodeToString(keyBytes));
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64Utils.decodeFromString(PRI_KEY));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(spec);
            // 数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());

            cipher.init(Cipher.DECRYPT_MODE, pubKey);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(pubKey);
            signature.update(data.getBytes("UTF-8"));
            return signature.verify(Base64Utils.decodeFromString(sign));
    }

    private static String extrPayload(String serial, String body) {
        String[] split = body.split(":");
        return split.length != 2 ? null : String.format("%s:%s", serial, split[0]);
    }

    private static String extrExpire(String body) {
        String[] split = body.split(":");
        return split.length != 2 ? null : split[0];
    }

    private static String extrSign(String body) {
        String[] split = body.split(":");
        // 去掉对齐符号
        return split.length != 2 ? null : split[1].replace("\u0000", "");
    }

    /**
     * 获取本机的序列号
     *
     * @return
     */
    public static String serial() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cat /sys/class/dmi/id/product_uuid");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String uuid = br.readLine();
            br.close();
            isr.close();
            is.close();

            if (StringUtils.hasText(uuid)) {
                return DigestUtils.md5Hex(uuid);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
