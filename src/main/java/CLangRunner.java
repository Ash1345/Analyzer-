import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLangRunner {

    public static String run(String sourceFile)
            throws Exception {

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "clang++",
                        "-Xclang",
                        "-ast-dump=json",
                        "-fsyntax-only",
                        sourceFile
                );

        processBuilder.redirectErrorStream(true);

        Process process =
                processBuilder.start();

        StringBuilder output =
                new StringBuilder();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                process.getInputStream()
                        )
                );

        String line;

        while ((line = reader.readLine()) != null) {

            output.append(line);
            output.append(System.lineSeparator());
        }

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new RuntimeException(
                    "Clang failed for: "
                            + sourceFile
                            + System.lineSeparator()
                            + output
            );
        }

        return output.toString();
    }

    public static void main(String[] args)
            throws Exception {

        String sourceFile =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp";

        String ast =
                run(sourceFile);

        System.out.println(
                "Clang analysis successful!"
        );

        System.out.println(
                "AST size: "
                        + ast.length()
                        + " characters"
        );
    }
}