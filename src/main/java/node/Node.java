package node;

import clone.Clone;
import block.Block;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.Setter;
import util.HashUtils;
import util.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static persistence.PersistenceUtils.deleteAllBLocks;
import static persistence.PersistenceUtils.insertBlock;
import static util.HttpUtils.GSON;
import static util.HttpUtils.HTTP_GET;
import static util.HttpUtils.HTTP_POST;
import static util.HttpUtils.RESPONSE_CODE_OK;
import static util.HttpUtils.createHttpUrl;
import static util.HttpUtils.createHttpUrlConnection;
import static util.HttpUtils.queryToMap;
import static util.NodeUtils.BLOCKS_GET_PATH;
import static util.NodeUtils.BLOCKS_PUSH_PATH;
import static util.NodeUtils.CLONES_PATH;
import static util.NodeUtils.IP_FIELD;
import static util.NodeUtils.PORT_FIELD;
import static util.NodeUtils.addNodeLog;
import static util.NodeUtils.cloneEqualsNode;
import static util.NodeUtils.getNodeDatabaseName;
import static util.NodeUtils.getNodeQueryParams;
import static util.NodeUtils.validateClone;

@Setter
@Getter
public class Node {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final Long SLEEP_TIME = 6000L;
    private static final Long CLONE_DISCOVERY_TIME = 10000L;
    private static final Long SYNC_SLEEP_TIME = 20000L;

    private final String ip;
    private final String port;
    private final List<Block> blocks = new ArrayList<>();
    private final List<Clone> clones = new ArrayList<>();

    public Node(String ip, String port) {
        this.ip = ip;
        this.port = port;

        logBlocks();
        logClones();
        findClones();
        synchronizeBlocks();
        addNodeLog(this, "started");
    }

    public void handlePushTransaction(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            var reader = new JsonReader(new InputStreamReader(requestBody));
            var block = serializeAndAddBlockFromTransactionIfValid(reader);
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] payload = SUCCESS.getBytes();
                exchange.sendResponseHeaders(HttpUtils.RESPONSE_CODE_OK, payload.length);
                os.write(payload);
            }
            addNodeLog(this, format("received transaction and created a block: %s", block));
            block.ifPresent(this::sendBlockToClonesIfPresent);
        }
    }

    public void handlePushBlock(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            var reader = new JsonReader(new InputStreamReader(requestBody));
            var block = serializeAndAddBlockIfValid(reader);
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] payload = SUCCESS.getBytes();
                exchange.sendResponseHeaders(HttpUtils.RESPONSE_CODE_OK, payload.length);
                os.write(payload);
            }
            addNodeLog(this, format("received block: %s", block));
            block.ifPresent(this::sendBlockToClonesIfPresent);
        }
    }

    public void handleGetClones(HttpExchange exchange) throws IOException {
        var response = GSON.toJson(clones);
        exchange.sendResponseHeaders(RESPONSE_CODE_OK, response.getBytes().length);
        Map<String, String> requesterIpAndPort = queryToMap(exchange.getRequestURI().getQuery());
        Clone clone = new Clone(requesterIpAndPort.get(IP_FIELD), requesterIpAndPort.get(PORT_FIELD));
        if (validateClone(clone) && !clones.contains(clone)) clones.add(clone);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes(UTF_8));
        }
    }

    private void sendBlockToClonesIfPresent(Block block) {
        try {
            sendBlockToClones(block);
        } catch (IOException e) {
            addNodeLog(this, format("Error sending block to clone: [%s]", e.getMessage()));
        }
    }

    private Optional<Block> serializeAndAddBlockFromTransactionIfValid(JsonReader reader) {
        Block block = GSON.fromJson(reader, Block.class);
        if (block.getTransaction() == null) {
            throw new RuntimeException("Transaction is missing");
        }
        block.setHash(HashUtils.hashBlock(block.getTransaction()));
        if (!blocks.contains(block)) {
            addBlockToLedger(block);
            return Optional.of(block);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Block> serializeAndAddBlockIfValid(JsonReader reader) {
        Block block = GSON.fromJson(reader, Block.class);
        if (block == null || block.getHash() == null || block.getTransaction() == null) {
            throw new RuntimeException("Block is invalid");
        }
        if (!blocks.contains(block)) {
            addBlockToLedger(block);
            return Optional.of(block);
        } else {
            return Optional.empty();
        }
    }

    private void addBlockToLedger(Block block) {
        blocks.add(block);
        insertBlock(getNodeDatabaseName(ip, port), block);
    }

    public void handleGetBlocks(HttpExchange exchange) throws IOException {
        var response = GSON.toJson(blocks);
        exchange.sendResponseHeaders(RESPONSE_CODE_OK, response.getBytes().length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes(UTF_8));
        }
    }

    private void sendBlockToClones(Block block) throws IOException {
        for (Clone clone: clones) {
            if (cloneEqualsNode(clone, this)) {
                continue;
            }
            var url = createHttpUrl(clone.getIp(), clone.getPort());
            addNodeLog(this, format("sending block to %s", url));
            byte[] payload = GSON.toJson(block).getBytes(UTF_8);
            var connection = createHttpUrlConnection(HTTP_POST, url + BLOCKS_PUSH_PATH, String.valueOf(payload.length));
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload, 0, payload.length);
            }
        }
    }

    private void getBlocksFromClones() throws IOException {
        addNodeLog(this, "querying blocks from all clones");
        List<List<Block>> result = getBlockListsFromClones();
        replaceBlocksIfNeeded(result);
    }

    private List<List<Block>> getBlockListsFromClones() throws IOException {
        List<List<Block>> result = new ArrayList<>();
        for (Clone clone: clones) {
            if (cloneEqualsNode(clone, this)) {
                continue;
            }
            var url = createHttpUrl(clone.getIp(), clone.getPort());
            addNodeLog(this, format("querying blocks from clone %s", url));
            var connection = createHttpUrlConnection(HTTP_GET, url + BLOCKS_GET_PATH);
            try (InputStream inputStream = connection.getInputStream()) {
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, UTF_8));
                Block[] receivedNodes = GSON.fromJson(reader, Block[].class);
                List<Block> blocks = List.of(receivedNodes);
                result.add(blocks);
            } catch (Exception e) {
                addNodeLog(this, format("Error getting block list: [%s]", e.getMessage()));
                result.add(new ArrayList<>());
            }
        }
        return result;
    }

    private void getClonesFromClones() throws IOException {
        addNodeLog(this, "querying clones from all clones");
        List<List<Clone>> result = getClonesListsFromClones();
        setNewClonesList(result);
    }

    private void setNewClonesList(List<List<Clone>> receivedClones) {
        List<Clone> result = receivedClones.stream().flatMap(List::stream).collect(toList());
        result.forEach(clone -> {
            if (!clones.contains(clone)) {
                clones.add(clone);
            }
        });
    }

    public void setInitialClones(List<Clone> initialClones) {
        clones.addAll(initialClones);
    }

    private List<List<Clone>> getClonesListsFromClones() throws IOException {
        List<List<Clone>> result = new ArrayList<>();
        for (Clone clone: clones) {
            if (cloneEqualsNode(clone, this)) {
                continue;
            }
            var url = createHttpUrl(clone.getIp(), clone.getPort());
            addNodeLog(this, format("querying clones from clone %s", url));
            var connection = createHttpUrlConnection(HTTP_GET, url + CLONES_PATH + "?" + getNodeQueryParams(this));
            try (InputStream inputStream = connection.getInputStream()) {
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, UTF_8));
                Clone[] receivedClones = GSON.fromJson(reader, Clone[].class);
                List<Clone> clones = List.of(receivedClones);
                result.add(clones);
            } catch (Exception e) {
                addNodeLog(this, format("Error getting clone list: [%s]", e.getMessage()));
                result.add(new ArrayList<>());
            }
        }
        return result;
    }

    private void replaceBlocksIfNeeded(List<List<Block>> result) {
        var largest = result.stream().max(comparing(List::size));
        largest.ifPresent(largestList -> {
            if (largestList.size() > this.blocks.size()) {
                refreshLedger(largestList);
            }
        });
    }

    private void refreshLedger(List<Block> largestList) {
        this.blocks.clear();
        this.blocks.addAll(largestList);
        deleteAllBLocks(getNodeDatabaseName(ip, port));
        largestList.forEach(b -> insertBlock(getNodeDatabaseName(ip, port), b));
    }

    public void populateBlockListWithDbData(List<Block> blockList) {
        this.blocks.clear();
        this.blocks.addAll(blockList);
    }

    private void findClones() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(CLONE_DISCOVERY_TIME);
                    getClonesFromClones();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void synchronizeBlocks() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SYNC_SLEEP_TIME);
                    getBlocksFromClones();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void logBlocks() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SLEEP_TIME);
                    addNodeLog(this, format("blocks: %s", blocks));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void logClones() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SLEEP_TIME);
                    addNodeLog(this, format("clones: %s", clones));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
