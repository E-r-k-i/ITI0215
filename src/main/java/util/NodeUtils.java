package util;

import clone.Clone;
import node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static util.HttpUtils.createHttpUrl;
import static util.HttpUtils.getUrlParams;

public class NodeUtils {

    public static final String CLONES_PATH = "/clones";
    public static final String BLOCKS_PUSH_PATH = "/blocks/push";
    public static final String BLOCKS_GET_PATH = "/blocks/get";
    public static final String TRANSACTION_PUSH_PATH = "/transaction/push";
    public static final String IP_FIELD = "ip";
    public static final String PORT_FIELD = "port";

    public static String getNodeQueryParams(Node node) {
        Map<String, String> params = new HashMap<>();
        params.put(IP_FIELD, node.getIp());
        params.put(PORT_FIELD, node.getPort());
        return getUrlParams(params);
    }

    public static boolean cloneEqualsNode(Clone clone, Node node) {
        return clone.getIp().equals(node.getIp()) && clone.getPort().equals(node.getPort());
    }

    public static boolean validateClone(Clone clone) {
        return clone.getIp() != null && clone.getPort() != null;
    }

    public static void addNodeLog(Node node, String message) {
        System.out.println();
        System.out.printf("%s -> { %s }%n", getNodeLogHeading(node), message);
        System.out.println();
    }

    private static String getNodeLogHeading(Node node) {
        return format("Node %s", createHttpUrl(node.getIp(), node.getPort()));
    }

    public static String getNodeDatabaseName(String ip, String port) {
        return format("%sledger%s", ip, port);
    }
}
