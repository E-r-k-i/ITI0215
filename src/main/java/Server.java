
import com.sun.net.httpserver.HttpServer;
import services.AddressService;
import services.BlockService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    public Server(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/addr", new AddressService(port));
        server.createContext("/api", new BlockService());
        server.start();

    }


}

