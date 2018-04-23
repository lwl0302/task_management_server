package com.mrray.desens.task.utils;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class String2ArrayConverter extends StdConverter<String, List<String>> {

    @Override
    public List<String> convert(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, "\n");
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        return list;
    }
}
