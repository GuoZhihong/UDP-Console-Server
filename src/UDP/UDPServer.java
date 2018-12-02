package UDP;

import Others.SocketServerThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPServer {
    private SocketAddress localAddress;
    private SocketAddress routerAddress;
    public UDPServer() {
        localAddress = new InetSocketAddress(8007);
        routerAddress = new InetSocketAddress("localhost",3000);
    }

    public void listenAndServer() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.bind(this.localAddress);
        SocketServerThread thread = new SocketServerThread(datagramChannel,this.routerAddress);
        thread.run();
    }

}
