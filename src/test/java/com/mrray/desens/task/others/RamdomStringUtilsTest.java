package com.mrray.desens.task.others;

import com.mrray.desens.task.utils.AESUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Arthur on 2017/7/26.
 */
public class RamdomStringUtilsTest {

    @Test
    public void test() throws Exception {

        System.out.println(UUID.randomUUID().toString());
        System.out.println(RandomStringUtils.random(8, true, true).toLowerCase());
        System.out.println(RandomStringUtils.random(8, true, false).toLowerCase());
        System.out.println(RandomStringUtils.random(8, false, true).toLowerCase());

    }

    @Test
    public void aes() throws Exception {

        String encrypt = AESUtils.encrypt("12345678123456781234567812345678", "tanghuan");
        System.out.println(encrypt);
        String res = AESUtils.decrypt("12345678123456781234567812345678", encrypt);
        System.out.println(res);

    }
}
