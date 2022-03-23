package address;

import com.google.gson.Gson;
import node.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CloneReader {

    public static Clone[] read(Node node) throws IOException {
        Gson gson = new Gson();
        var path = node.getPort().equals("8500") ? "resources/conf.json" : "resources/conf2.json";

        try (InputStream is = CloneReader.class.getClassLoader().getResourceAsStream(path)) {
            assert is != null;
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            return gson.fromJson(inputStreamReader, Clone[].class);
        }
    }
}