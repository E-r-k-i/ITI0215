
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import services.AddressService;
import services.BlockService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Map;

public class Server {

    public Server(int port) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/addr", new AddressService(port, readConf()));
        server.createContext("/api", new BlockService());
        server.start();

        //infinite loop to poll with 4 sec interval
        while (1==1){
            //print all polled hosts
            System.out.println(PollNeighbours.pollNeighbours(readConf()));
            Thread.sleep(4000);
        }

    }

    // returns know hosts form conf file
    public static String readConf() throws IOException {
        // create a map
        String result = "";
        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            // convert JSON file to map
            Map<?, ?> map = mapper.readValue(Paths.get("C:/Users/Erki/IdeaProjects/ITI0215/Ledger/src/main/java/resources/conf.json").toFile(), Map.class);
            // print map entries
            String[] hosts = new String[0];
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result = entry.getKey() + ":" + entry.getValue();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}

