import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ProjectAnalyzer {

    public static void main(String[] args)
            throws Exception {

        String projectDirectory =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project";

        ObjectMapper mapper =
                new ObjectMapper();

        List<String> sourceFiles =
                ProjectScanner.findSourceFiles(
                        projectDirectory
                );

        System.out.println(
                "========== PROJECT ANALYSIS =========="
        );

        System.out.println(
                "Source files found: "
                        + sourceFiles.size()
        );

        for (String sourceFile :
                sourceFiles) {

            System.out.println();
            System.out.println(
                    "--------------------------------------"
            );

            System.out.println(
                    "Analyzing: "
                            + sourceFile
            );

            String astJson =
                    CLangRunner.run(
                            sourceFile
                    );

            AstNode root =
                    mapper.readValue(
                            astJson,
                            AstNode.class
                    );

            System.out.println(
                    "AST loaded successfully"
            );

            System.out.println(
                    "Root kind: "
                            + root.kind
            );

            List<CodeEntity> entities =
                    EntityAnalyzer.analyze(
                            root,
                            sourceFile
                    );

            System.out.println(
                    "Entities found: "
                            + entities.size()
            );

            for (CodeEntity entity :
                    entities) {

                System.out.println(
                        "  "
                                + entity.kind
                                + " : "
                                + entity.qualifiedName
                                + " | "
                                + entity.file
                                + ":"
                                + entity.line
                                + ":"
                                + entity.column
                );
            }
        }

        System.out.println();
        System.out.println(
                "========== PROJECT ANALYSIS COMPLETE =========="
        );
    }
}