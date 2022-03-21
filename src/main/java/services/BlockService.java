package services;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BlockService implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {
                postRequest(exchange);
            } else if (method.equals("GET")) {
                getRequest(exchange);
            } else {
                System.out.println("Invalid method " + method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postRequest(HttpExchange exchange) throws IOException {
        String buf = inputToString(exchange);
        exchange.getResponseHeaders().add("Content-Type", "text/xml");
        exchange.sendResponseHeaders(200, (buf.length()));
        OutputStream os = exchange.getResponseBody();
        os.write(getBytes(buf));
        System.out.println(buf);
        os.close();

    }

    public static void getRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String response;
        if(requestURI.getQuery() != null){      // if query variable is present
            response = requestURI.getQuery();
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            System.out.println("param A=" + params.get("A") + "param B=" + params.get("B"));
        }else{                                  //if no query variable, go to path parameter response
            response = requestURI.toString();
        }

        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }

    // POST input to string
    public static String inputToString (HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        int b;
        StringBuilder buf = new StringBuilder();
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }
        br.close();
        isr.close();
        return buf.toString();
    }


    public static byte[] getBytes(String string){
        byte[] bytes = string.getBytes();
        return bytes;
    }


    public static Map<String, String> queryToMap(String query) {
        if(query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }

}
