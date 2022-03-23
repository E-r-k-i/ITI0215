
import com.sun.net.httpserver.HttpServer;
import node.Node;
import node.NodeProcessHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static persistence.PersistenceUtils.createTableIfNotExists;
import static persistence.PersistenceUtils.queryBlocks;
import static util.NodeUtils.BLOCKS_GET_PATH;
import static util.NodeUtils.BLOCKS_PUSH_PATH;
import static util.NodeUtils.CLONES_PATH;
import static util.NodeUtils.TRANSACTION_PUSH_PATH;
import static util.NodeUtils.getIpAddress;
import static util.NodeUtils.getNodeDatabaseName;

public class Main {

    public static void main(String[] args) throws IOException {
        var port = "8500";
        if (args.length > 0) {
            port = args[0];
        }

        var ip = getIpAddress();

        createTableIfNotExists(getNodeDatabaseName(ip, port));

        Node node = new Node(ip, port);
        new NodeProcessHandler(node);
        node.populateBlockListWithDbData(queryBlocks(getNodeDatabaseName(ip, port)));

        //List<Clone> possibleClones = List.of(new Clone(ip, "8500"), new Clone(ip, "9000"), new Clone(ip, "9001"));
        node.setInitialClones(Stream.of(CloneReader.read(node)).collect(toList()));

        HttpServer server = HttpServer.create(new InetSocketAddress(parseInt(port)), 0);
        server.createContext(CLONES_PATH, node::handleGetClones);
        server.createContext(BLOCKS_PUSH_PATH, node::handlePushBlock);
        server.createContext(BLOCKS_GET_PATH, node::handleGetBlocks);
        server.createContext(TRANSACTION_PUSH_PATH, node::handlePushTransaction);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

}
