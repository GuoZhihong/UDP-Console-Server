package Others;

import UDP.Packet;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SocketServerThread extends Thread{
    private DatagramChannel datagramChannel;
    private SocketAddress routerAddress;
    private boolean debugMessage;
    private String directoryPath;
    private int port;
    private HashMap<String,String> query = new HashMap<>();

    public void setPort(int port) {
        this.port = port;
    }

    public void setDebugMessage(boolean debugMessage) {
        this.debugMessage = debugMessage;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
    public SocketServerThread(DatagramChannel datagramChannel,SocketAddress routerAddress) {
        this.routerAddress = routerAddress;
        this.datagramChannel = datagramChannel;
        this.debugMessage = false;
        this.directoryPath = null;
        this.port = datagramChannel.socket().getLocalPort();
    }

    public void run() {
        readInput();
        try {
            handleRequest(this.datagramChannel,this.routerAddress);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void readInput(){
        Scanner inputStream = new Scanner(System.in);
        String input = inputStream.nextLine();
        if(!input.isEmpty() && input.contains("httpfs")){
            if (input.contains("-p")) {
                String temp = input.substring(input.indexOf("-p") + 3);
                String portNumber = temp.substring(0, temp.indexOf(" ", 0));
                this.setPort(Integer.valueOf(portNumber));
            }
            if(input.contains("-v")){
                this.setDebugMessage(true);
            }
            if(input.contains("-d")){
                this.setDirectoryPath(input.substring(input.indexOf("-d") + 3));
            }
        }else {
            System.out.println("Invalid command");
            System.exit(0);
        }
    }
    private void handleRequest(DatagramChannel datagramChannel,SocketAddress routerAddress) throws IOException, InterruptedException {
        while (true) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
            byteBuffer.clear();
            datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
            Packet packet = Packet.fromBuffer(byteBuffer);

            if(packet.getType() == 0) {
                System.out.println("Three-way handshake is done, server is connected");
                packet.toBuilder().setType(1);
                String payload = new String(packet.getPayload(), UTF_8);

                if (debugMessage) {
                    System.out.println("Here are the debug messages:");
                    System.out.println(payload);
                    System.out.println();
                }
                String header = null;
                String body = null;
                header = payload.split("\r\n\r\n")[0];
                if (header.contains("POST")) {
                    body = payload.split("\r\n\r\n")[1];
                }
                Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber() + 1).setType(1).setPayload(response(header, body).getBytes()).create();
                this.datagramChannel.send(response.toBuffer(), routerAddress);
            }else {
                threeWayHandShake(packet);
            }
        }
    }
    private void threeWayHandShake(Packet packet) throws IOException {

        System.out.println("Handshaking #1 SYN packet has received");
        System.out.println("Message is : " + new String(packet.getPayload(), StandardCharsets.UTF_8));
        String testString = "Hi";
        Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber() + 1).setType(3).setPayload(testString.getBytes()).create();
        this.datagramChannel.send(response.toBuffer(), routerAddress);
        System.out.println("Handshaking #2 SYN packet has sent out");

    }


    private String response(String header,String body) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(status(header)+"\r\n");

        if(status(header).contains("200")&&status(header).contains("OK")) {
            stringBuilder.append("Connection: keep-alive\r\n");
        }else {
            stringBuilder.append(header.split("\r\n")[1]+"\r\n");
        }
        for (int i = 2; i < header.split("\r\n").length; i++) {
            stringBuilder.append(header.split("\r\n")[i]+"\r\n");
        }
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String path = firstLine[1];
        if(!path.contains("get")&&!path.contains("post")) {
            stringBuilder.append(contentType(header));
        }
        stringBuilder.append("\r\n\r\n");
        if(status(header).contains("200")&&status(header).contains("OK")) {
            stringBuilder.append(locateFiles(header, body)+"\r\n");
        }
        if(path.contains("get")||path.contains("post")){//normal request as A1
            stringBuilder.append(output(header,body));
        }
        return stringBuilder.toString();
    }

    private String status(String header) throws IOException {
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String path = firstLine[1];

        if(path.contains("get")||path.contains("post")){//normal request as A1
            return "HTTP/1.0 200 OK";
        }
        URL url = new URL(path);
        String fileName = url.getFile();
        File file = new File(this.directoryPath+"\\"+fileName);
        if((file.exists()||fileName.equals("")&&header.contains("GET")) || header.contains("POST")){
//            if(!file.canRead()&&header.contains("GET")){
//                return "HTTP/1.0 ERROR 500";
//            }
            return "HTTP/1.0 200 OK";
        }else {
            return "HTTP/1.0 ERROR 404";
        }
    }

    /*A1 normal request*/
    private String output(String header,String body) throws MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder();
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String path = firstLine[1];
        URL url = new URL(path);
        if(path.contains("?")) {
            queryParameters(url);
        }
        stringBuilder.append("{\r\n");
        stringBuilder.append("  \"args\": {\r\n");
        for (String key:query.keySet()) {
            stringBuilder.append("      ").append("\""+key+"\"").append("\""+query.get(key)+"\"").append(",\r\n");
        }
        stringBuilder.append("  },\r\n");
        if(header.contains("POST")){
            stringBuilder.append("  \"data\": ").append("\""+body+"\"\r\n");
        }
        stringBuilder.append("  \"headers\": {\r\n");
        for (int i = 2; i < header.split("\r\n").length; i++) {
            stringBuilder.append("      ").append(header.split("\r\n")[i]+"\r\n");
        }
        stringBuilder.append("  },\r\n");
        stringBuilder.append("  \"url\": ").append("\""+url+"\"\r\n");
        stringBuilder.append("}\r\n");
        query.clear();
        return stringBuilder.toString();
    }

    private void queryParameters(URL u){
        String queryLine = u.getQuery();
        String [] pair = queryLine.split("&");
        for (String s:pair) {
            String [] rest = s.split("=");
            query.put(rest[0],rest[1]);
        }
    }

    private synchronized String locateFiles(String header, String body) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String path = firstLine[1];
        if(path.contains("get")||path.contains("post")){
            return "";
        }
        URL url = new URL(path);
        String fileName = url.getFile();
        File file = new File(directoryPath+"\\"+fileName);
        if(header.contains("GET")){
            if(fileName.equals("")){
                File[]fileList = file.listFiles((dir, name) -> name.charAt(0) != '.');
                stringBuilder.append("Here are the files in this directory:\r\n");
                for (File f:fileList
                ) {
                    stringBuilder.append(f.getName()+"\r\n");
                }
            }else {
                FileOperation fileOperation = new FileOperation();
                stringBuilder.append(fileOperation.readFile(this.directoryPath +"\\"+ fileName));
            }
        }else if(header.contains("POST")){
            FileOperation fileOperation = new FileOperation();
            fileOperation.writeFile(body, this.directoryPath+"\\"+fileName);
        }
        return stringBuilder.toString();
    }
    private String contentType(String header){
        StringBuilder stringBuilder = new StringBuilder();
        String[] firstLine = header.split("\r\n")[0].split(" ");
        String path = firstLine[1];
        String type = path.substring(path.indexOf(".")+ 1);
        switch (type){
            case "html":
                stringBuilder.append("Content-Type: text/html\r\n");
                stringBuilder.append("Content-Disposition: inline\r\n");
                break;
            case "json":{
                stringBuilder.append("Content-Type: application/json\r\n");
                stringBuilder.append("Content-Disposition: inline\r\n");
                break;
            }
            case "txt":{
                stringBuilder.append("Content-Type: text/plain\r\n");
                stringBuilder.append("Content-Disposition: inline\r\n");
                break;
            }
            default:{
                stringBuilder.append("Content-Type: text/plain\r\n");
                stringBuilder.append("Content-Disposition: attachment\r\n");
            }
        }
        return stringBuilder.toString();
    }
}
