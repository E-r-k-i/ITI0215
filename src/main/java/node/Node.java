package node;

import block.Block;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.Setter;
import util.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static util.HttpUtils.GSON;
import static util.HttpUtils.HTTP_POST;
import static util.HttpUtils.RESPONSE_CODE_OK;
import static util.HttpUtils.createHttpUrl;
import static util.HttpUtils.createHttpUrlConnection;
import static util.NodeUtils.addNodeLog;

@Setter
@Getter
public class Node {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final Long SLEEP_TIME = 10000L;

    private final String ip;
    private final String port;
    private final List<Block> blocks = new ArrayList<>();
    private List<Clone> clones = new ArrayList<>();

    public Node(String ip, String port) {
        this.ip = ip;
        this.port = port;

        logBlocks();
        logClones();
        addNodeLog(this, "started");
    }

    public void handlePush(HttpExchange exchange) throws IOException {
        addNodeLog(this, "received http request");
        try (InputStream requestBody = exchange.getRequestBody()) {
            var reader = new JsonReader(new InputStreamReader(requestBody));
            var block = serializeAndAddBlockIfValid(reader);
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] payload = SUCCESS.getBytes();
                exchange.sendResponseHeaders(HttpUtils.RESPONSE_CODE_OK, payload.length);
                os.write(payload);
            }
            addNodeLog(this, format("received block: %s", block));
            // TODO: 22.03.2022 is it needed here to send blocks away? Maybe do it in some Thread task...
            block.ifPresent(b -> {
                try {
                    sendBlockToClones(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Optional<Block> serializeAndAddBlockIfValid(JsonReader reader) {
        Block block = GSON.fromJson(reader, Block.class);
        if (block == null || block.getContent() == null) {
            throw new RuntimeException("Block is invalid");
        }
        if (!blocks.contains(block)) {
            blocks.add(block);
            return Optional.of(block);
        } else {
            return Optional.empty();
        }
    }

    public void handleGetBlocks(HttpExchange exchange) throws IOException {
        addNodeLog(this, "received http request");
        var response = GSON.toJson(blocks);
        exchange.sendResponseHeaders(RESPONSE_CODE_OK, response.getBytes().length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes(UTF_8));
        }
    }

    private void sendBlockToClones(Block block) throws IOException {
        for (Clone clone: clones) {
            var url = createHttpUrl(clone.getIp(), clone.getPort());
            addNodeLog(this, format("sending block to %s", url));
            byte[] payload = GSON.toJson(block).getBytes(UTF_8);
            var connection = createHttpUrlConnection(HTTP_POST, url + "/blocks/push", String.valueOf(payload.length));
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload, 0, payload.length);
            }
            handleSendBlockResponse(connection, block, clone);
        }
    }

    private void handleSendBlockResponse(HttpURLConnection connection, Block block, Clone clone) throws IOException {
        var cloneUrl = createHttpUrl(clone.getIp(), clone.getPort());
        var message = format(getResponse(connection).equals(SUCCESS) ? "successfully sent %s to %s" : "error sending %S to %s", block, cloneUrl);
        addNodeLog(this, message);
    }

    private String getResponse(HttpURLConnection connection) throws IOException {
        String result;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            result = content.toString();
        }
        return result;
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
