package Others;

import Others.FileOperation;
import UDP.UDPClient;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class ClientLibrary {

    private UDPClient client;
    private String host;
    private int port;

    /*use specific data structure to store required information*/
    private HashMap<String,String> header = new HashMap<>();
    private String body;
    private HashMap<String,String> query = new HashMap<>();

    public ClientLibrary(UDPClient client) {
        this.client = client;
    }

    public String GETorPOST(String allow,String str) throws MalformedURLException{

        /*to get host and port from given URL*/
        String url = str.substring(str.indexOf("http://"), str.indexOf("'",str.indexOf("http://")));
        URL u = new URL(url);
        this.host = u.getHost();
        this.port = u.getPort();

        if(str.contains("?")) {
            queryParameters(u);//handle query parameters
        }
        String response = null;
        try {
            if(allow.equals("GET")) {
                response = buildResponse(client.run(new InetSocketAddress(host, port), buildRequest("GET", str, url)), str);
            }else if(allow.equals("POST")){
                response = buildResponse(client.run(new InetSocketAddress(host,port), buildRequest("POST",str,url)),str);
            }
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    private void queryParameters(URL u){
        String queryLine = u.getQuery();
        String [] pair = queryLine.split("&");
        for (String s:pair) {
            String [] rest = s.split("=");
            query.put(rest[0],rest[1]);
        }
    }

    private String buildRequest(String allow, String str,String url) throws IOException{

        /*default information setting(Notice : not all of http header field definitions are defined,only those that appeared in the assignment are defined)*/
        String connectionType = "close";
        String userAgent = "COMP445";
        String contentType = null;
        String contentLength = null;
        FileOperation fileOperation = new FileOperation();
        if(allow.equals("POST")) {//these options are only valid under POST request
            contentType = "text/plain";
            if(str.contains("-d")) {
                body = str.substring(str.indexOf("{", str.indexOf("-d")), str.indexOf("}") + 1);
            }else if(str.contains("-f")){
                String path = str.substring(str.indexOf("-f") + 3,str.indexOf(" ",str.indexOf("-f") + 3));
                body = fileOperation.readFile(path);
            }
            contentLength = String.valueOf(body.length());
        }

        /*initial headers to hash-map*/
        header.put("Host",this.host);
        header.put("Connection",connectionType);
        header.put("User-Agent",userAgent);
        if(allow.equals("POST")) {
            header.put("Content-Length", contentLength);
            header.put("Content-Type", contentType);
        }

        /*-h Header requirement:support multiple headers add or update*/
        String temp = str;
        String key;
        String value;
        for (int i = 0; i < str.length(); i++) {
            if(!temp.contains("-h")){
                break;
            }else{
                i = temp.indexOf("-h") + 3;
                temp = temp.substring(i);
                key = temp.substring(0,temp.indexOf(":",0));
                value = temp.substring(temp.indexOf(":",temp.indexOf(key))+ 1,temp.indexOf(" ",temp.indexOf(key)));
                if(header.containsKey(key)){
                    header.replace(key,value);
                    continue;
                }
                header.put(key,value);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(allow + " " + url + " HTTP/1.0\r\n");
        for (String keys:header.keySet()) {
            stringBuilder.append(keys).append(": ").append(header.get(keys)).append("\r\n");
        }
        if(allow.equals("POST")){
            stringBuilder.append("\r\n").append(body);
        }else if(allow.equals("GET")){
            stringBuilder.append("\r\n");
        }
        return stringBuilder.toString();
    }

    /*str: whole command line input*/
    private String buildResponse(String payload, String str) throws IOException{


        FileOperation fileOperation = new FileOperation();
        String response = payload;

        /*verbose requirement*/
        if(str.contains("-v")) {//case that needs verbose
            if(needOutputFile(str)){//case need to output body data
                fileOperation.writeFile(response,str.substring(str.indexOf("-o") + 3));
            }
            return response;
        }else {//case that does not need verbose
            response = response.substring(response.indexOf("{"),response.lastIndexOf("}")+ 1);
            if(needOutputFile(str)){//case need to output body data
                fileOperation.writeFile(response,str.substring(str.indexOf("-o") + 3));
            }
        }
        return response;
    }

    private boolean needOutputFile(String str){
        if(str.contains("-o")){
            return true;
        }
        return false;
    }


    /*method to determine if the http response needs a redirection or not */
    private boolean needRedirection(String data){
        data = data.substring(0,20);//this is due to status will always be the first line, 0-20 characters for approximation of it.
        if(data.contains("300")||data.contains("301")||data.contains("302")||data.contains("304")){//satisfy any of those will need redirect
            return true;
        }
        return false;
    }
}
