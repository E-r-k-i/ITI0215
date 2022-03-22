import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;

public class PollNeighbours {

    // returns current host's known hosts and neighbours' known hosts
    public static String pollNeighbours(String knownHosts) throws IOException {
        String[] hosts = knownHosts.split(":", 2);
        //System.out.println(hosts[1]);
        String allHosts = hosts[1].substring(1, hosts[1].length() - 1);
        hosts = hosts[1].substring(1, hosts[1].length() - 1).split(",");
        for (String host : hosts) {
            host = host.trim();
            allHosts += attemptConnection(host);
        }
        return allHosts;
    }

    // connect to neighbour to get known hosts
    private static String attemptConnection(String host) throws IOException, ConnectException {
            String hosts = "";
            URL url = new URL("http://"+host+"/addr");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    hosts = line.split(":", 2)[1];
                    hosts = ", " + hosts.substring(1, hosts.length() - 1);
                }
            } catch (ConnectException ex) {
                System.out.println(ex);
            }
        return hosts;
    }
}
