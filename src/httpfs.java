import UDP.UDPServer;
import java.io.*;
public class httpfs {

    public static void main(String[] args) throws IOException {
        listenAndServe();
    }


    private static void listenAndServe() throws IOException {
        UDPServer server = new UDPServer();
        server.listenAndServer();
    }
}
