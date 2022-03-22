import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

public class PollNeighbours {

    public static String pollNeighbours(String knownHosts) throws IOException {
        String[] hosts = knownHosts.split(":", 2);
        //System.out.println(hosts[1]);
        String allHosts = "";
        hosts = hosts[1].substring(1, hosts[1].length() - 1).split(",");
        for (String host : hosts) {
            host = host.trim();
            System.out.println(host);
            allHosts += attemptConnection(host);
        }
        System.out.println(allHosts);
        return allHosts;
    }

    private static String attemptConnection(String host) throws IOException, ConnectException {
            String hosts = "";
            URL url = new URL("http://localhost:8511/addr");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    hosts = line;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        return hosts;
    }
}
