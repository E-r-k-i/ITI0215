
import com.sun.net.httpserver.HttpServer;
import node.Clone;
import node.Node;
import services.AddressService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static util.NodeUtils.getIpAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8511;
        String ip = "localhost";
        if (args.length > 0) {
            port = parseInt(args[0]);
        }

        var realIp = getIpAddress();

        Node node = new Node(ip, valueOf(port));

        List<Clone> possibleClones = List.of(new Clone(ip, "8500"), new Clone(ip, "9000"), new Clone(ip, "9001"));
        node.setClones(possibleClones.stream().filter(clone -> !clone.getPort().equals(args[0])).collect(Collectors.toList()));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/addr", new AddressService(port));
        server.createContext("/blocks/push", node::handlePush);
        server.createContext("/blocks/get", node::handleGetBlocks);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
