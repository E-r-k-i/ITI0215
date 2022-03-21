package services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


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

            writeIp(getIp(exchange));

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

    private void writeIp(String ip) {
        //todo
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
        String response = requestURI.toString();

        //System.out.println(response);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }

}
