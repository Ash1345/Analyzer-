import java.util.ArrayList;
import java.util.List;

public class ProjectAnalyzer {

    // =========================================================
    // Analyze project
    // =========================================================

    public AnalysisResult analyze(
            String projectDirectory)
            throws Exception {

        // =====================================================
        // AST output directory
        // =====================================================

        String astOutputDirectory =
                projectDirectory
                        + "\\generated-ast";


        // =====================================================
        // Find source files
        // =====================================================

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


        // =====================================================
        // Generate AST for every source file
        // =====================================================

        List<String> astPaths =
                new ArrayList<>();


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


            // -------------------------------------------------
            // Generate AST
            // -------------------------------------------------

            String astPath =
                    ClangRunner.generateAst(
                            sourceFile,
                            astOutputDirectory
                    );


            astPaths.add(
                    astPath
            );


            System.out.println(
                    "AST generated: "
                            + astPath
            );
        }


        // =====================================================
        // Load project
        // =====================================================

        ProjectLoader loader =
                new ProjectLoader();


        Project project =
                loader.load(
                        astPaths,
                        sourceFiles
                );


        System.out.println();

        System.out.println(
                "Project loaded successfully."
        );

        System.out.println(
                "Translation units: "
                        + project
                        .getTranslationUnits()
                        .size()
        );


        // =====================================================
        // Build graph
        // =====================================================

        CodeGraph graph =
                GraphBuilder.build(
                        project
                );


        // =====================================================
        // Calculate analysis statistics
        // =====================================================

        AnalysisStatistics statistics =
                AnalysisStatisticsCalculator.calculate(
                        project,
                        graph
                );


        // =====================================================
        // Print graph statistics
        // =====================================================

        System.out.println();

        System.out.println(
                "Number of entities: "
                        + graph.entities.size()
        );

        System.out.println(
                "Number of relationships: "
                        + graph.relationships.size()
        );

        System.out.println(
                "Number of evidence items: "
                        + statistics.getEvidenceCount()
        );


        // =====================================================
        // Print complete graph
        // =====================================================

        graph.printGraph();


        // =====================================================
        // Return analysis result
        // =====================================================

        return new AnalysisResult(
                project,
                graph,
                statistics
        );
    }


    // =========================================================
    // Main method
    // =========================================================

    public static void main(String[] args)
            throws Exception {

        // =====================================================
        // Project directory
        // =====================================================

        String projectDirectory =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project";


        // =====================================================
        // Create analyzer
        // =====================================================

        ProjectAnalyzer analyzer =
                new ProjectAnalyzer();


        // =====================================================
        // Analyze project
        // =====================================================

        AnalysisResult result =
                analyzer.analyze(
                        projectDirectory
                );


        // =====================================================
        // Get graph from analysis result
        // =====================================================

        CodeGraph graph =
                result.getGraph();


        // =====================================================
        // Get statistics
        // =====================================================

        AnalysisStatistics statistics =
                result.getStatistics();


        // =====================================================
        // Verify graph
        // =====================================================

        GraphVerifier.verify(
                graph
        );


        // =====================================================
        // Print analysis statistics
        // =====================================================

        System.out.println();

        System.out.println(
                "========== ANALYSIS STATISTICS =========="
        );

        System.out.println(
                "Source files: "
                        + statistics.getSourceFileCount()
        );

        System.out.println(
                "Translation units: "
                        + statistics.getTranslationUnitCount()
        );

        System.out.println(
                "Entities: "
                        + statistics.getEntityCount()
        );

        System.out.println(
                "Relationships: "
                        + statistics.getRelationshipCount()
        );

        System.out.println(
                "Evidence items: "
                        + statistics.getEvidenceCount()
        );


        // =====================================================
        // Analysis complete
        // =====================================================

        System.out.println();

        System.out.println(
                "========== PROJECT ANALYSIS COMPLETE =========="
        );
    }
}