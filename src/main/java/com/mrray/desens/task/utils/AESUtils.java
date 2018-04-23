package com.mrray.desens.task.utils;

import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class AESUtils {

    public static String encrypt(String key, String value) {
        try {

            StringBuilder builder = new StringBuilder(value);
            int n = (16 - (value.length() % 16));
            for (int i = 0; i < n; i++) {
                builder.append("\u0000");
            }
            value = builder.toString();

            IvParameterSpec iv = new IvParameterSpec("5689452317595236".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64Utils.encodeToString(encrypted);
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String encrypted) {
        try {

            IvParameterSpec iv = new IvParameterSpec("5689452317595236".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64Utils.decodeFromString(encrypted));

            return new String(original).replace("\u0000", "");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return null;
    }

}
