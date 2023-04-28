import java.io.*;

public class IOExceptionDemo {
    public static void main(String[] args) {
        File file = new File("nonexistent_file.txt");
        FileReader fr = new FileReader(file);
    }
}