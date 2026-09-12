import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;


import java.io.File;

public class AstReader {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        File astFile = new File(
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\ast.json"
        );

        AstNode root =
                mapper.readValue(
                        astFile,
                        AstNode.class
                );

        System.out.println("Ast loaded successfully!");

        System.out.println(
                "Root kind: " + root.kind
        );

        System.out.println(
                "Number of children: " +
                        (root.inner == null
                                ? 0
                                : root.inner.size())
        );

        System.out.println();
        System.out.println("========== AST ==========");

        AstWalker.walk(root, 0);

        System.out.println();
        System.out.println("========== CODE ANALYSIS ==========");

        CodeAnalyzer.analyze(root);

        // Build complete graph
        CodeGraph graph =
                GraphBuilder.build(root);

        System.out.println();
        System.out.println("========== CODE GRAPH ==========");

        System.out.println(
                "Number of entities: "
                        + graph.entities.size()
        );

        System.out.println(
                "Number of relationships: "
                        + graph.relationships.size()
        );

        graph.printGraph();

        System.out.println();
        System.out.println("========== WHO USES Calculator::add? ==========");

        CodeEntity addMethod = null;

        for (CodeEntity entity : graph.entities) {

            if ("METHOD".equals(entity.kind)
                    && "Calculator::add".equals(entity.qualifiedName)) {

                addMethod = entity;
                break;
            }
        }

        if (addMethod != null) {

            List<Relationship> incomingRelationships =
                    graph.findRelationshipsTo(addMethod.id);

            for (Relationship relationship :
                    incomingRelationships) {

                System.out.println(
                        relationship.source
                                + " --"
                                + relationship.type
                                + "--> "
                                + relationship.target
                );
            }
        }



        System.out.println();
        System.out.println("========== MAIN RELATIONSHIPS ==========");

        CodeEntity mainEntity = null;

        for (CodeEntity entity : graph.entities) {

            if ("FUNCTION".equals(entity.kind)
                    && "main".equals(entity.name)) {

                mainEntity = entity;
                break;
            }
        }

        if (mainEntity != null) {

            List<Relationship> mainRelationships =
                    graph.findRelationshipsFrom(mainEntity.id);

            for (Relationship relationship :
                    mainRelationships) {

                System.out.println(
                        relationship.source
                                + " --"
                                + relationship.type
                                + "--> "
                                + relationship.target
                );
            }
        }

        System.out.println();
        System.out.println("========== ENTITIES ==========");

        for (CodeEntity entity :
                graph.entities) {

            System.out.println(
                    entity.kind
                            + " : "
                            + entity.qualifiedName
            );
        }

        System.out.println();
        System.out.println("========== GRAPH QUERY TEST ==========");

        System.out.println("Classes:");

        for (CodeEntity entity :
                graph.findEntitiesByKind("CLASS")) {

            System.out.println(
                    entity.kind + " : " +
                            entity.qualifiedName
            );
        }

        System.out.println();
        System.out.println("Entities named Calculator:");

        for (CodeEntity entity :
                graph.findEntitiesByName("Calculator")) {

            System.out.println(
                    entity.kind + " : " +
                            entity.qualifiedName
            );
        }

    }
}