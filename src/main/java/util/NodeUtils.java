package util;

import node.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import static java.lang.String.format;
import static util.HttpUtils.createHttpUrl;

public class NodeUtils {

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

    public static void addNodeLog(Node node, String message) {
        System.out.println();
        System.out.printf("%s -> { %s }%n", getNodeLogHeading(node), message);
        System.out.println();
    }

    private static String getNodeLogHeading(Node node) {
        return format("Node %s", createHttpUrl(node.getIp(), node.getPort()));
    }
}
