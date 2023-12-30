package org.hw_http;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String path, Map<String, String> queryParams) {
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public static Map<String, String> parseQueryParams(String path) {
        Map<String, String> queryParams = new HashMap<>();
        int questionMarkIndex = path.indexOf('?');
        if (questionMarkIndex != -1 && questionMarkIndex < path.length() - 1) {
            String queryString = path.substring(questionMarkIndex + 1);
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }

    public static String extractPathWithoutQuery(String path) {
        int questionMarkIndex = path.indexOf('?');
        return (questionMarkIndex != -1) ? path.substring(0, questionMarkIndex) : path;
    }
}

