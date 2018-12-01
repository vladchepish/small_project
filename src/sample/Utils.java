package sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static void writeToFile(String fileName, String text) {
        FileWriter writeFile = null;
        try {
            File logFile = new File(fileName);
            writeFile = new FileWriter(logFile);
            writeFile.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writeFile != null) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
