package util;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class HttpUtils {

    public static Gson GSON = new Gson();
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final int RESPONSE_CODE_OK = 200;
    public static final int RESPONSE_CODE_BAD_REQUEST = 400;

    private static final String URL_FORMAT = "http://%s:%s";

    public static String createHttpUrl(String ip, String port) {
        return format(URL_FORMAT, ip, port);
    }

    public static String getUrlParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }

    public static HttpURLConnection createHttpUrlConnection(String method, String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        connection.setRequestProperty(ACCEPT, APPLICATION_JSON);
        connection.setDoOutput(true);
        return connection;
    }

    public static HttpURLConnection createHttpUrlConnection(String method,
                                                      String url,
                                                      String contentLength) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        connection.setRequestProperty(ACCEPT, APPLICATION_JSON);
        connection.setRequestProperty(CONTENT_LENGTH, contentLength);
        connection.setDoOutput(true);
        return connection;
    }

    public static ResponseDataDto getResponseDataDto(String message, int code) {
        var data = new ResponseDataDto();
        data.setCode(code);
        byte[] payload = message.getBytes();
        data.setPayload(payload);
        data.setLen(payload.length);
        return data;
    }
}
