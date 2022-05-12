package io.nuvalence.platform.audit.service.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Utility class to read sample JSON files from classpath in order to populate test data.
 */
public class SamplesUtil {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    private SamplesUtil() {}

    public static InputStream getJsonFile(String path) {
        return SamplesUtil.class.getResourceAsStream(path);
    }

    /**
     * Reads json file from resources and maps it to an object.
     * @param path resource path
     * @param clazz object class
     * @param <T> object type
     * @return file contents
     * @throws IOException when the object cannot be mapped
     */
    public static <T> T readJsonFile(String path, Class<T> clazz) throws IOException {
        try (InputStream input = getJsonFile(path)) {
            return objectMapper.readValue(input, clazz);
        }
    }

    /**
     * Reads json file from resources as a string.
     * @param path resource path
     * @return file contents
     * @throws IOException when the file cannot opened and/or closed
     */
    public static String readJsonFileAsString(String path) throws IOException {
        try (InputStream input = getJsonFile(path)) {
            try (BufferedReader br =
                    new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
