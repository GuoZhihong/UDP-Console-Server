package UDP;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;


import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {

    private SocketAddress localAddress;
    private SocketAddress   routerAddress;
    private boolean isHandShaken = false;
    public UDPClient() {
        this.localAddress = new InetSocketAddress(41830);
        this.routerAddress = new InetSocketAddress("localhost",3000);
    }

    public UDPClient(int localPort){
        this.localAddress = new InetSocketAddress(localPort);
        this.routerAddress = new InetSocketAddress("localhost",3000);
    }

    public String run(InetSocketAddress serverAddress,String message) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.bind(this.localAddress);
        long newSeq = threeWayHandShake(datagramChannel,serverAddress);
        if(this.isHandShaken) {
            Packet p = null;
            if(message.getBytes().length <= Packet.MAX_DATA) {
                p = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(newSeq + 1)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(message.getBytes())
                        .create();
            }else {
                return "Data is too long for a single packet";
            }
            datagramChannel.send(p.toBuffer(), routerAddress);
            timer(datagramChannel,p);

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            this.routerAddress = datagramChannel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            if (resp.getType() == 1) {
                String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
                return payload;
            }
        }
        return null;
    }
    private void timer(DatagramChannel datagramChannel,Packet packet) throws IOException {
        // Try to receive a packet within timeout.
        datagramChannel.configureBlocking(false);
        Selector selector = Selector.open();
        datagramChannel.register(selector, OP_READ);
        selector.select(1000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            datagramChannel.send(packet.toBuffer(), routerAddress);
            timer(datagramChannel,packet);
        }
        keys.clear();
        return;
    }

    private long threeWayHandShake(DatagramChannel datagramChannel,InetSocketAddress serverAddress) throws IOException {

        String testString = "Hi S";
        Packet test = new Packet.Builder()
                .setType(2)
                .setSequenceNumber(1L)
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload(testString.getBytes())
                .create();
        datagramChannel.send(test.toBuffer(), routerAddress);
        System.out.println("Handshaking #1 SYN packet has already sent out");

        timer(datagramChannel,test);


        ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
        byteBuffer.clear();
        datagramChannel.receive(byteBuffer);
        byteBuffer.flip();
        Packet packet = Packet.fromBuffer(byteBuffer);

        System.out.println("Message from server is :"+new String(packet.getPayload(), StandardCharsets.UTF_8));
        this.isHandShaken = true;
        System.out.println("Three-way handshake is done, data will start transferring");
        System.out.println("\r\n");
        return packet.getSequenceNumber();
    }
}
