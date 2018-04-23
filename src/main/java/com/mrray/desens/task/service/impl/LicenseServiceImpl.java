package com.mrray.desens.task.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mrray.desens.task.entity.dto.AppLicenseDto;
import com.mrray.desens.task.entity.vo.RespBody;
import com.mrray.desens.task.service.LicenseService;
import com.mrray.desens.task.utils.LicenseUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class LicenseServiceImpl implements LicenseService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseServiceImpl.class);


    /**
     * 检查程序的激活状态
     * @return
     */
    @Override
    public RespBody checkActiveStatus() {

        JSONObject object = new JSONObject();
        object.put("active", false);

        try {
            String licFile = LicenseUtils.LICENSE;

            File license = new File("./", licFile);

            // 如果License文件不存在 或者没有内容 说明未激活
            if (!license.exists() || license.length() == 0) {
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            String serial = LicenseUtils.serial();
            if (!StringUtils.hasText(serial)) {
                return new RespBody<>().setData(object).setMessage("serial not found");
            }

            logger.info("serial: {}", serial);

            List<String> lines = FileUtils.readLines(license, Charset.forName("UTF-8"));

            if (lines == null || lines.size() == 0) {
                logger.error("can't read license from License File");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            String licenseStr = lines.get(0);
            logger.info("license: {}", licenseStr);

            // 校验License是否正确
            if (!LicenseUtils.verifyLicense(serial, licenseStr)) {
                logger.error("校验失败");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            // 是否过期
            if (LicenseUtils.isExpire(serial, licenseStr)) {
                logger.error("license 过期");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            String expire = LicenseUtils.expireTime(serial, licenseStr);
            if (expire == null || !StringUtils.hasText(expire)) {
                logger.error("没有过期时间");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            object.put("active", true);
            object.put("expire", expire);
            logger.info("过期时间：{}", expire);
            return new RespBody<>().setData(object).setMessage("active success");

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return new RespBody<>().setData(object).setMessage("active failed");
    }

    /**
     * 获取程序的序列
     * @return
     */
    @Override
    public RespBody getAppSerial() {
        String serial = LicenseUtils.serial();
        if (!StringUtils.hasText(serial)) {
            return new RespBody<>().setMessage("serial not found");
        }
        JSONObject object = new JSONObject();
        object.put("serial", serial);
        return new RespBody<>().setData(object);
    }

    /**
     * 激活程序
     * @param dto
     * @return
     */
    @Override
    public RespBody activeApp(AppLicenseDto dto) {

        JSONObject object = new JSONObject();
        object.put("active", false);
        try {

            String serial = LicenseUtils.serial();
            if (!StringUtils.hasText(serial)) {
                return new RespBody<>().setData(object).setMessage("serial not found");
            }
            String license = dto.getLicense();

            if (!LicenseUtils.verifyLicense(serial, license)) {
                logger.info("license verify failed");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            File licFile = new File("./", LicenseUtils.LICENSE);
            File parent = licFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            String expire = LicenseUtils.expireTime(serial, license);
            if (expire == null || !StringUtils.hasText(expire)) {
                logger.info("expire no content");
                return new RespBody<>().setData(object).setMessage("active failed");
            }

            object.put("active", true);
            object.put("expire", expire);

            ArrayList<String> licenses = new ArrayList<>();
            licenses.add(license);

            FileUtils.writeLines(licFile, licenses, false);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return new RespBody<>().setData(object).setMessage("active failed");
        }

        return new RespBody<>().setData(object).setMessage("active success");
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAppActiveStatus() {
        try {
            File licFile = new File("./", LicenseUtils.LICENSE);
            if (!licFile.exists()) {
                logger.warn("未激活");
                return;
            }
            List<String> lines = FileUtils.readLines(licFile, Charset.forName("UTF-8"));

            if (lines == null || lines.size() == 0) {
                logger.warn("未激活");
                return;
            }

            String serial = LicenseUtils.serial();
            if (!StringUtils.hasText(serial)) {
                logger.warn("获取序列号失败");
                return;
            }

            String license = lines.get(0);
            if (license == null || !StringUtils.hasText(license)) {
                logger.warn("未激活");
                return;
            }

            if (!LicenseUtils.verifyLicense(serial, license)) {
                logger.warn("未激活");
                return;
            }

            // 是否过期
            if (LicenseUtils.isExpire(serial, license)) {
                logger.warn("未激活");
                return;
            }

            logger.info("已经激活");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

}
