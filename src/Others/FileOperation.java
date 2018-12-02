package Others;

import java.io.*;

public class FileOperation {
    public String readFile(String path) throws IOException {
        File file = new File(path);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String nextLine;
        do {
            nextLine = bufferedReader.readLine();
            stringBuilder.append(nextLine);
        } while (nextLine != null);
        return stringBuilder.toString();
    }

    public void writeFile(String body, String filePath){
        File file = new File(filePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(body);
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
