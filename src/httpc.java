import Others.ClientLibrary;
import UDP.UDPClient;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class httpc {
    public static void main(String[] args) throws MalformedURLException, UnknownHostException {
        System.out.println("Please input your options:(1) single-thread (2) multi-threads");
        Scanner inputStream = new Scanner(System.in);
        int option = inputStream.nextInt();
        if(option == 1){
            inputStream = new Scanner(System.in);
            System.out.println("Please input your curl");
            String input = inputStream.nextLine();
            readInput(input,1);
        }else {
            inputStream = new Scanner(System.in);
            System.out.println("Please input your first curl");
            String input = inputStream.nextLine();
            System.out.println("Please input your Second curl");
            String input2 = inputStream.nextLine();
            readInput(input,1);
            readInput(input2,2);
        }
    }
    private static void readInput(String input,int clientNumber) throws MalformedURLException, UnknownHostException {
        UDPClient client = null;
        if(clientNumber == 1) {
            client = new UDPClient();
        }else {
            client = new UDPClient(41831);
        }
        ClientLibrary clientLibrary = new ClientLibrary(client);
        if(!input.isEmpty() && input.contains("httpc")){
            if(!input.contains("help")){
                if(input.contains("get")){
                    System.out.println(clientLibrary.GETorPOST("GET",input));
                }else if(input.contains("post")){
                    System.out.println(clientLibrary.GETorPOST("POST",input));
                }else {
                    System.out.println("Invalid command");
                    System.exit(0);
                }
            }
            else{
                help(input);
            }
        }else {
            System.out.println("Invalid command");
            System.exit(0);
        }
    }

    public static void help(String string){
        if(string.contains("get")){

            System.out.println("usage: httpc get [-v] [-h key:value] URL");
            System.out.println("Get executes a HTTP GET request for a given URL.");
            System.out.println("    -v  Prints the detail of the response such as protocol, status, and headers.");
            System.out.println("    -h  key:value Associates headers to HTTP Request with the format 'key:value'.");

        }else if(string.contains("post")){

            System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL");
            System.out.println("Post executes a HTTP POST request for a given URL with inline data or from file.");
            System.out.println("    -v  Prints the detail of the response such as protocol, status, and headers.");
            System.out.println("    -h  key:value Associates headers to HTTP Request with the format 'key:value'.");
            System.out.println("    -d  string Associates an inline data to the body HTTP POST request.");
            System.out.println("    -f  file Associates the content of a file to the body HTTP POST request.");
            System.out.println("Either [-d] or [-f] can be used but not both.");

        }else {

            System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
            System.out.println("Usage:");
            System.out.println("    httpc command [arguments]");
            System.out.println("The commands are:");
            System.out.println("    get     executes a HTTP GET request and prints the response.");
            System.out.println("    post    executes a HTTP POST request and prints the response.");
            System.out.println("    help    prints this screen.");
            System.out.println();
            System.out.println("Use \"httpc help [command]\" for more information about a command.");

        }
    }
}
