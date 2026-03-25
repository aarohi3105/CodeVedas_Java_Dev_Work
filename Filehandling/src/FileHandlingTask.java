import java.io.*;

public class FileHandlingTask {

    public static void main(String[] args) {

        String inputFile = "src/input.txt";
        String outputFile = "output.txt";

        int lineCount = 0;
        int wordCount = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line;

            while ((line = br.readLine()) != null) {
                lineCount++;

                String words[] = line.trim().split("\\s+");
                if (!line.trim().isEmpty()) {
                    wordCount += words.length;
                }
            }

            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write("File Processing Result\n");
            bw.write("----------------------\n");
            bw.write("Total Lines: " + lineCount + "\n");
            bw.write("Total Words: " + wordCount + "\n");

            bw.close();

            System.out.println("Processing completed. Output written to " + outputFile);

        } catch (FileNotFoundException e) {
            System.out.println("Input file not found!");
        } catch (IOException e) {
            System.out.println("Error reading or writing file!");
        }
    }
}