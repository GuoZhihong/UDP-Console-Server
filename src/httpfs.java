import Others.SocketServerThread;
import UDP.UDPServer;

import java.io.*;
import java.nio.channels.DatagramChannel;

public class httpfs {

    public static void main(String[] args) throws IOException {
        listenAndServe();
    }


    private static void listenAndServe() throws IOException {
        UDPServer server = new UDPServer();
        server.listenAndServer();
    }
}
