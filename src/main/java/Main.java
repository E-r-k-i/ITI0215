
import java.io.*;

import static java.lang.Integer.parseInt;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = 9000;
        if (args.length > 0) {
            port = parseInt(args[0]);
        }
    }

}
