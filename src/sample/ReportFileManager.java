package sample;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReportFileManager {

    public static File createAndCleanReportFile(String reportFileName) {
        File reportFile = new File(reportFileName);
        try{
            FileWriter writer = new FileWriter(reportFile);
            // Перезаписываем содержимое файла на случай, если файл не пуст
            writer.write("");
            writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return reportFile;
    }

    public static void addIformationToFile(File reportFile, String string){
        try{
            FileWriter writer = new FileWriter(reportFile, true);
            writer.write(string + "\n");
            writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static List<String> addReportFileIntoOneList(File reportFile) throws IOException {
        FileReader fr = new FileReader(reportFile);
        Scanner scanedFile = new Scanner(fr);
        List<String> finalList = new ArrayList<>();
        while (scanedFile.hasNextLine()){
            finalList.add(scanedFile.nextLine());
        }
        fr.close();
        return finalList;
    }
}
