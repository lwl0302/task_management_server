package com.mrray.desens.task.utils;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;

public class Array2StringConverter extends StdConverter<List<String>, String> {

    @Override
    public String convert(List<String> values) {
        return String.join("\n", values);
    }
}
