package util;

import clone.Clone;
import node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.String.format;
import static util.HttpUtils.createHttpUrl;

public class NodeUtils {

    public static final String CLONES_PATH = "/clones";
    public static final String BLOCKS_PUSH_PATH = "/blocks/push";
    public static final String BLOCKS_GET_PATH = "/blocks/get";
    public static final String TRANSACTION_PUSH_PATH = "/transaction/push";

    public static String getIpAddress() throws UnknownHostException {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("CURRENT IP ADDRESS IS : " + ip);
            return ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean cloneEqualsNode(Clone clone, Node node) {
        return clone.getIp().equals(node.getIp()) && clone.getPort().equals(node.getPort());
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
