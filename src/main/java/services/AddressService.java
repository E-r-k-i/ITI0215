package services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URL;


public class AddressService implements HttpHandler {

    private static String hosts;
    private final int port;

    public AddressService(int port, String hosts) {
        this.port = port;
        this.hosts = hosts;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            System.out.println(getIp(exchange));

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

    private void attemptConnection(String host) throws IOException {

        URL url = new URL("http://localhost:6600/addr");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                System.out.println(line);
            }
        }

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
        String response = hosts;
        //requestURI.toString()

        //System.out.println(response);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        System.out.print(response);
        os.write(response.getBytes());
        os.close();

    }

}
