
import com.sun.net.httpserver.HttpServer;
import node.Node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static address.CloneReader.read;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static persistence.PersistenceUtils.createTableIfNotExists;
import static persistence.PersistenceUtils.queryBlocks;
import static util.NodeUtils.getIpAddress;
import static util.NodeUtils.getNodeDatabaseName;

public class Main {

    private static final String CLONES_PATH = "/clones";
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
        //List<Clone> possibleClones = List.of(new Clone(ip, "8500"), new Clone(ip, "9000"), new Clone(ip, "9001"));
        node.setClones(Stream.of(read(node)).collect(toList()));

        HttpServer server = HttpServer.create(new InetSocketAddress(parseInt(port)), 0);
        //server.createContext(CLONES_PATH, new AddressReader(parseInt(port)));
        server.createContext(BLOCKS_PUSH_PATH, node::handlePush);
        server.createContext(BLOCKS_GET_PATH, node::handleGetBlocks);
        server.createContext(TRANSACTION_PUSH_PATH, node::handleTransaction);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
