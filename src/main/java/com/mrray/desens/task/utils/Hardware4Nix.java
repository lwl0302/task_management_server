package com.mrray.desens.task.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class Hardware4Nix {

    private static final Logger logger = LoggerFactory.getLogger(Hardware4Nix.class);

    private static String sn = null;

    public static String getSerialNumber() {

        if (sn == null) {
            readDmidecode();
        }
        if (sn == null) {
            readLshal();
        }
        if (sn == null) {
            throw new RuntimeException("Cannot find computer SN");
        }

        return sn;
    }

    private static BufferedReader read(String command) {

        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(command.split(" "));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        assert process != null;
        try(
            InputStream is = process.getInputStream()
        ) {
            return new BufferedReader(new InputStreamReader(is));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static void readDmidecode() {

        String line;
        String marker = "Serial Number:";

        try(
            BufferedReader br = read("dmidecode -t system")
        ) {
            assert br != null;
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(marker)[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void readLshal() {

        String line;
        String marker = "system.hardware.serial =";

        try (
            BufferedReader br  = read("lshal")
        ){
            assert br != null;
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(marker)[1].replaceAll("\\(string\\)|(\\')", "").trim();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
