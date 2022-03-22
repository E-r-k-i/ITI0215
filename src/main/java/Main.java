
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
import static persistence.PersistenceUtils.createTableIfNotExists;
import static persistence.PersistenceUtils.queryBlocks;
import static util.HttpUtils.createHttpUrl;
import static util.NodeUtils.getIpAddress;
import static util.NodeUtils.getNodeDatabaseName;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8511;
        String ip = "localhost";
        if (args.length > 0) {
            port = parseInt(args[0]);
        }

        // TODO: 22.03.2022 local and public ip address issue. Basically we need to use public ip, to make connections between local machine and remote servers
        var realIp = getIpAddress();

        createTableIfNotExists(getNodeDatabaseName(realIp, valueOf(port)));

        Node node = new Node(realIp, valueOf(port));
        node.populateBlockListWithDbData(queryBlocks(getNodeDatabaseName(realIp, valueOf(port))));


        List<Clone> possibleClones = List.of(new Clone(realIp, "8500"), new Clone(realIp, "9000"), new Clone(realIp, "9001"));
        node.setClones(possibleClones.stream().filter(clone -> !clone.getPort().equals(args[0])).collect(Collectors.toList()));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/addr", new AddressService(port));
        server.createContext("/blocks/push", node::handlePush);
        server.createContext("/blocks/get", node::handleGetBlocks);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
