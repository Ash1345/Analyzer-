import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProjectScanner {

    public static List<String> findSourceFiles(
            String projectDirectory)
            throws IOException {

        List<String> sourceFiles =
                new ArrayList<>();

        Path projectPath =
                Path.of(projectDirectory);

        try (Stream<Path> paths =
                     Files.walk(projectPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(ProjectScanner::isSourceFile)
                    .forEach(path ->
                            sourceFiles.add(
                                    path.toAbsolutePath()
                                            .toString()
                            )
                    );
        }

        return sourceFiles;
    }

    private static boolean isSourceFile(
            Path path) {

        String fileName =
                path.getFileName()
                        .toString()
                        .toLowerCase();

        return fileName.endsWith(".cpp")
                || fileName.endsWith(".cc")
                || fileName.endsWith(".cxx");
    }

    public static void main(String[] args)
            throws Exception {

        String projectDirectory =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project";

        List<String> sourceFiles =
                findSourceFiles(
                        projectDirectory
                );

        System.out.println(
                "========== SOURCE FILES =========="
        );

        for (String file :
                sourceFiles) {

            System.out.println(file);
        }

        System.out.println();
        System.out.println(
                "Total source files: "
                        + sourceFiles.size()
        );
    }
}