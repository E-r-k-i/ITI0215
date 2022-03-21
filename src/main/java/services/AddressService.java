package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class AddressService implements HttpHandler {

    private final int port;

    public AddressService(int port) {
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            System.out.println(getIp(exchange));

            getNeighbours(getIp(exchange));

            if (method.equals("GET")){
                getRequest(exchange);
            }
            else {
                System.out.println("Invalid method " + method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private void getNeighbours(String ip) throws IOException {
        // create a map
        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();

            // convert JSON file to map
            Map<?, ?> map = mapper.readValue(Paths.get("C:/Users/Erki/IdeaProjects/ITI0215/Ledger/src/main/java/resources/conf.json").toFile(), Map.class);
            // print map entries
            String[] hosts = new String[0];
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
                hosts = (entry.getValue().toString()).substring(1, entry.getValue().toString().length()-1).split(",");
            }
            for(String host : hosts){
                host = host.trim();
                System.out.println(host);
                attemptConnection(host);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void attemptConnection(String host) throws IOException {

        String command = "curl -X GET "+host+"/addr";
        //System.out.println(command);
        //Process process = Runtime.getRuntime().exec(command);
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process process = pb.start();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder responseStrBuilder = new StringBuilder();

        String line = new String();

        while ((line = br.readLine()) != null) {
            System.out.println("read line from curl command: " + line);
            responseStrBuilder.append(line);
        }
        is.close();

    }


    private String getIp(HttpExchange exchange){

        String hostString = exchange.getRemoteAddress().getHostString();
        if (hostString.contains("0:0:0:0")){
            hostString = "localhost:";
        }
        String ip = String.valueOf(port);
        return hostString+ip;
    }

    private static void getRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String response = requestURI.toString()+"asd";

        //System.out.println(response);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }

}
