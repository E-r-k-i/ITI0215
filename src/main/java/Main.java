
import com.sun.net.httpserver.HttpServer;
import node.Clone;
import node.Node;
import services.AddressService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static persistence.PersistenceUtils.createTableIfNotExists;
import static persistence.PersistenceUtils.queryBlocks;
import static util.NodeUtils.getIpAddress;
import static util.NodeUtils.getNodeDatabaseName;

public class Main {

    private static final String ADDRESS_PATH = "/addr";
    private static final String BLOCKS_PUSH_PATH = "/blocks/push";
    private static final String BLOCKS_GET_PATH = "/blocks/get";
    private static final String TRANSACTION_PUSH_PATH = "/transaction/push";

    public static void main(String[] args) throws IOException {
        var port = "8500";
        if (args.length > 0) {
            port = args[0];
        }

        var ip = getIpAddress();

        createTableIfNotExists(getNodeDatabaseName(ip, port));

        Node node = new Node(ip, port);
        node.populateBlockListWithDbData(queryBlocks(getNodeDatabaseName(ip, port)));

        // node discovery not implemented yet todo: add
        List<Clone> possibleClones = List.of(new Clone(ip, "8500"), new Clone(ip, "9000"), new Clone(ip, "9001"));
        node.setClones(possibleClones.stream().filter(clone -> !clone.getPort().equals(args[0])).collect(toList()));

        HttpServer server = HttpServer.create(new InetSocketAddress(parseInt(port)), 0);
        server.createContext(ADDRESS_PATH, new AddressService(parseInt(port)));
        server.createContext(BLOCKS_PUSH_PATH, node::handlePush);
        server.createContext(BLOCKS_GET_PATH, node::handleGetBlocks);
        server.createContext(TRANSACTION_PUSH_PATH, node::handleTransaction);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
