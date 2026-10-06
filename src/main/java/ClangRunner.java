import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClangRunner {

    public static String generateAst(
            String sourceFile,
            String outputDirectory)
            throws Exception {

        // =====================================================
        // Validate source file
        // =====================================================

        if (sourceFile == null
                || sourceFile.isBlank()) {

            throw new IllegalArgumentException(
                    "Source file cannot be null or blank"
            );
        }

        // =====================================================
        // Create output directory
        // =====================================================

        Path outputPath =
                Path.of(outputDirectory);

        Files.createDirectories(
                outputPath
        );

        // =====================================================
        // Create AST output file name
        // =====================================================

        String sourceFileName =
                Path.of(sourceFile)
                        .getFileName()
                        .toString();

        String astFileName =
                sourceFileName
                        + ".json";

        Path astFilePath =
                outputPath.resolve(
                        astFileName
                );

        // =====================================================
        // Run Clang
        // =====================================================

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "clang++",
                        "-Xclang",
                        "-ast-dump=json",
                        "-fsyntax-only",
                        sourceFile
                );

        processBuilder.redirectErrorStream(
                true
        );

        Process process =
                processBuilder.start();

        // =====================================================
        // Read Clang output
        // =====================================================

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

            output.append(
                    System.lineSeparator()
            );
        }

        // =====================================================
        // Wait for Clang
        // =====================================================

        int exitCode =
                process.waitFor();

        // =====================================================
        // Handle Clang failure
        // =====================================================

        if (exitCode != 0) {

            throw new RuntimeException(
                    "Clang failed for: "
                            + sourceFile
                            + System.lineSeparator()
                            + output
            );
        }

        // =====================================================
        // Write AST JSON to file
        // =====================================================

        Files.writeString(
                astFilePath,
                output.toString()
        );

        return astFilePath
                .toAbsolutePath()
                .toString();
    }


    // =========================================================
    // Test
    // =========================================================

    public static void main(String[] args)
            throws Exception {

        String sourceFile =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp";

        String outputDirectory =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\generated-ast";

        String astPath =
                generateAst(
                        sourceFile,
                        outputDirectory
                );

        System.out.println(
                "Clang analysis successful!"
        );

        System.out.println(
                "AST generated at:"
        );

        System.out.println(
                astPath
        );

        File astFile =
                new File(astPath);

        System.out.println();

        System.out.println(
                "AST size: "
                        + astFile.length()
                        + " bytes"
        );
    }
}