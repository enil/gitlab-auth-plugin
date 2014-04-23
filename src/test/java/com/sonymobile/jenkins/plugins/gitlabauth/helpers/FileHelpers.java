package com.sonymobile.jenkins.plugins.gitlabauth.helpers;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Utility for loading JSON files from the test resources.
 *
 * @author Emil Nilsson
 */
public class FileHelpers {
    /**
     * Loads a JSON object from a file.
     *
     * The variant is added as a suffix after a "_" to the path if set and extension ".json".
     * The path file must be relative to the src/test/resources/__files directory used by WireMock.
     *
     * @param relativePath the path relative to src/test/resources/__files excluding the file extension
     * @param variant      the name of the variant suffix or null
     * @return a JSON object
     * @throws IOException if the file couldn't be loaded
     */
    public static JSONObject loadJsonObject(String relativePath, String variant) throws IOException {
        if (isNotEmpty(variant)) {
            relativePath += "_" + variant;
        }
        relativePath = "/__files/" + relativePath + ".json";

        URL url = FileHelpers.class.getResource(relativePath);
        if (url == null) {
            throw new FileNotFoundException("The file " + relativePath + " doesn't exist in test resources");
        }

        // load JSON object
        return new JSONObject(FileUtils.readFileToString(new File(url.getFile())));
    }

    /**
     * Loads a JSON object from a file.
     *
     * @param relativePath the path relative to src/test/resources/__files excluding the file extension
     * @return a JSON object
     * @throws IOException if the file couldn't be loaded
     */
    public static JSONObject loadJsonObject(String relativePath) throws IOException {
        return loadJsonObject(relativePath, null);
    }
}
