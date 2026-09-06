import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLangRunner {

    public static void main(String[] args) throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "clang++",
                "-Xclang",
                "-ast-dump",
                "-fsyntax-only",
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp"
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();

        System.out.println();
        System.out.println("Clang exit code: " + exitCode);
    }
}