import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class AstReader {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        File astFile = new File(
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\ast.json"
        );

        AstNode root = mapper.readValue(astFile, AstNode.class);

        AstIndex index = new AstIndex();
        index.build(root);

        System.out.println("Ast loaded successfully!");
        System.out.println("Root kind: " + root.kind);
        System.out.println("Number of children: " +
                (root.inner == null ? 0 : root.inner.size()));

        System.out.println();
        System.out.println("========== AST ==========");

        AstWalker.walk(root, 0);

        System.out.println();
        System.out.println("========== CODE ANALYSIS ==========");

        CodeAnalyzer.analyze(root);

        System.out.println();
        System.out.println("========== CALL ANALYSIS ==========");

        CallAnalyzer.analyze(root,index);
    }
}