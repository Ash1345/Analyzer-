import java.util.Arrays;
import java.util.List;

public class AstReader {

    public static void main(String[] args) throws Exception {

        // =====================================================
        // Load project
        // =====================================================

        ProjectLoader loader =
                new ProjectLoader();

        Project project =
                loader.load(
                        Arrays.asList(
                                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\ast.json",
                                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\calculator_ast.json"
                        ),
                        Arrays.asList(
                                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp",
                                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\Calculator.cpp"
                        )
                );


        // =====================================================
        // Build graph from project
        // =====================================================

        CodeGraph graph =
                GraphBuilder.build(
                        project
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

        graph.printGraph();


        // =====================================================
        // Find main function
        // =====================================================

        CodeEntity mainFunction =
                graph.findEntityById(
                        "FUNCTION:main()"
                );

        if (mainFunction == null) {
            System.out.println();
            System.out.println(
                    "Could not find main function."
            );
            return;
        }


        // =====================================================
        // Test graph query:
        // relationships originating from main
        // =====================================================

        System.out.println();

        System.out.println(
                "========== OUTGOING RELATIONSHIPS FROM main =========="
        );

        List<Relationship> outgoing =
                graph.findRelationshipsFrom(
                        mainFunction.id
                );

        for (Relationship relationship :
                outgoing) {

            System.out.println(
                    relationship.source
                            + " --"
                            + relationship.type
                            + "--> "
                            + relationship.target
            );
        }


        // =====================================================
        // Test graph query:
        // entities related to main
        // =====================================================

        System.out.println();

        System.out.println(
                "========== ENTITIES RELATED TO main =========="
        );

        List<CodeEntity> relatedEntities =
                graph.findRelatedEntities(
                        mainFunction.id
                );

        for (CodeEntity entity :
                relatedEntities) {

            System.out.println(
                    entity.kind
                            + " : "
                            + entity.qualifiedName
            );
        }


        // =====================================================
        // Test graph query:
        // entities called by main
        // =====================================================

        System.out.println();

        System.out.println(
                "========== ENTITIES CALLED BY main =========="
        );

        List<CodeEntity> calledEntities =
                graph.findRelatedEntitiesByRelationshipType(
                        mainFunction.id,
                        "CALLS"
                );

        for (CodeEntity entity :
                calledEntities) {

            System.out.println(
                    entity.kind
                            + " : "
                            + entity.qualifiedName
            );
        }


        // =====================================================
        // Test graph query:
        // entities calling Calculator::add(int,int)
        // =====================================================

        System.out.println();

        System.out.println(
                "========== ENTITIES CALLING Calculator::add(int,int) =========="
        );

        CodeEntity calculatorAdd =
                graph.findEntityById(
                        "METHOD:Calculator::add(int,int)"
                );

        if (calculatorAdd != null) {

            List<CodeEntity> callers =
                    graph.findEntitiesRelatedTo(
                            calculatorAdd.id,
                            "CALLS"
                    );

            for (CodeEntity entity :
                    callers) {

                System.out.println(
                        entity.kind
                                + " : "
                                + entity.qualifiedName
                );
            }
        }


        // =====================================================
        // Test graph query:
        // entities reachable from main
        // =====================================================

        System.out.println();

        System.out.println(
                "========== ENTITIES REACHABLE FROM main VIA CALLS =========="
        );

        List<CodeEntity> reachable =
                graph.findReachableEntities(
                        mainFunction.id,
                        "CALLS"
                );

        for (CodeEntity entity :
                reachable) {

            System.out.println(
                    entity.kind
                            + " : "
                            + entity.qualifiedName
            );
        }


        // =====================================================
        // Test graph query:
        // path from main to helper
        // =====================================================

        System.out.println();

        System.out.println(
                "========== PATH FROM main TO helper =========="
        );

        CodeEntity helper =
                graph.findEntityById(
                        "FUNCTION:helper()"
                );

        if (helper != null) {

            GraphPath path =
                    graph.findPath(
                            mainFunction.id,
                            helper.id,
                            "CALLS"
                    );

            if (path != null) {

                for (int i = 0;
                     i < path.entities.size();
                     i++) {

                    CodeEntity entity =
                            path.entities.get(i);

                    System.out.print(
                            entity.qualifiedName
                    );

                    if (i < path.relationships.size()) {

                        Relationship relationship =
                                path.relationships.get(i);

                        System.out.print(
                                " --"
                                        + relationship.type
                                        + "--> "
                        );
                    }
                }

                System.out.println();

            } else {

                System.out.println(
                        "No CALLS path found."
                );
            }

        } else {

            System.out.println(
                    "Could not find helper function."
            );
        }
    }
}