import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class AstReader {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        File mainAstFile =
                new File(
                        "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\ast.json"
                );

        File calculatorAstFile =
                new File(
                        "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\calculator_ast.json"
                );

        // Read main.cpp AST
        AstNode mainRoot =
                mapper.readValue(
                        mainAstFile,
                        AstNode.class
                );

        // Read Calculator.cpp AST
        AstNode calculatorRoot =
                mapper.readValue(
                        calculatorAstFile,
                        AstNode.class
                );

        System.out.println(
                "Both AST files loaded successfully!"
        );

        System.out.println(
                "Main AST root: "
                        + mainRoot.kind
        );

        System.out.println(
                "Calculator AST root: "
                        + calculatorRoot.kind
        );


        // All translation units
        List<AstNode> roots =
                Arrays.asList(
                        mainRoot,
                        calculatorRoot
                );

        // Corresponding source files
        List<String> sourceFiles =
                Arrays.asList(
                        "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp",
                        "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\Calculator.cpp"
                );


        // Build one graph from both ASTs
        CodeGraph graph =
                GraphBuilder.build(
                        roots,
                        sourceFiles
                );


        System.out.println();

        System.out.println(
                "Number of entities: "
                        + graph.entities.size()
        );

        System.out.println(
                "Number of relationships: "
                        + graph.relationships.size()
        );

        graph.printGraph();
    }
}