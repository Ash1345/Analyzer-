import java.util.List;

public class GraphVerifier {

    public static void verify(CodeGraph graph) {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "Graph cannot be null"
            );
        }

        System.out.println();
        System.out.println(
                "========== GRAPH VALIDATION =========="
        );

        verifyGraphStructure(graph);

        System.out.println();
        System.out.println(
                "========== FUNCTIONAL VERIFICATION =========="
        );

        verifyCalculatorRelationships(graph);

        System.out.println();
        System.out.println(
                "========== GRAPH VERIFICATION COMPLETE =========="
        );
    }


    // =========================================================
    // Generic graph validation
    // =========================================================

    private static void verifyGraphStructure(
            CodeGraph graph) {

        verifyEntities(graph);

        verifyRelationships(graph);

        verifyEvidence(graph);

        System.out.println();
        System.out.println(
                "Graph structure validation successful."
        );
    }


    // =========================================================
    // Entity validation
    // =========================================================

    private static void verifyEntities(
            CodeGraph graph) {

        int checkedEntities = 0;

        for (CodeEntity entity :
                graph.entities) {

            if (entity == null) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Graph contains a null entity"
                );
            }

            if (entity.id == null
                    || entity.id.isBlank()) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Entity has no valid ID"
                );
            }

            checkedEntities++;
        }

        System.out.println(
                "PASS: All entities have valid IDs"
                        + " (" + checkedEntities + " checked)"
        );
    }


    // =========================================================
    // Relationship validation
    // =========================================================

    private static void verifyRelationships(
            CodeGraph graph) {

        int checkedRelationships = 0;

        for (Relationship relationship :
                graph.relationships) {

            if (relationship == null) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Graph contains a null relationship"
                );
            }

            if (relationship.sourceId == null
                    || relationship.sourceId.isBlank()) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Relationship has no valid source ID"
                );
            }

            if (relationship.targetId == null
                    || relationship.targetId.isBlank()) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Relationship has no valid target ID"
                );
            }

            if (relationship.type == null
                    || relationship.type.isBlank()) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Relationship has no valid type"
                );
            }


            // -------------------------------------------------
            // Verify that source entity actually exists
            // -------------------------------------------------

            CodeEntity source =
                    graph.findEntityById(
                            relationship.sourceId
                    );

            if (source == null) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Relationship source does not exist: "
                                + relationship.sourceId
                );
            }


            // -------------------------------------------------
            // Verify that target entity actually exists
            // -------------------------------------------------

            CodeEntity target =
                    graph.findEntityById(
                            relationship.targetId
                    );

            if (target == null) {

                throw new RuntimeException(
                        "VERIFICATION FAILED: "
                                + "Relationship target does not exist: "
                                + relationship.targetId
                );
            }

            checkedRelationships++;
        }

        System.out.println(
                "PASS: All relationships are valid"
                        + " (" + checkedRelationships + " checked)"
        );
    }


    // =========================================================
    // Evidence validation
    // =========================================================

    private static void verifyEvidence(
            CodeGraph graph) {

        int evidenceCount = 0;

        for (Relationship relationship :
                graph.relationships) {

            if (relationship.evidence == null) {
                continue;
            }

            for (RelationshipEvidence evidence :
                    relationship.evidence) {

                if (evidence == null) {

                    throw new RuntimeException(
                            "VERIFICATION FAILED: "
                                    + "Relationship contains null evidence"
                    );
                }

                if (evidence.file == null
                        || evidence.file.isBlank()) {

                    throw new RuntimeException(
                            "VERIFICATION FAILED: "
                                    + "Evidence has no source file"
                    );
                }

                if (evidence.line == null
                        || evidence.line <= 0) {

                    throw new RuntimeException(
                            "VERIFICATION FAILED: "
                                    + "Evidence has invalid line number"
                    );
                }

                if (evidence.column == null
                        || evidence.column <= 0) {

                    throw new RuntimeException(
                            "VERIFICATION FAILED: "
                                    + "Evidence has invalid column number"
                    );
                }

                evidenceCount++;
            }
        }

        System.out.println(
                "PASS: All relationship evidence is valid"
                        + " (" + evidenceCount + " checked)"
        );
    }


    // =========================================================
    // Functional verification
    // =========================================================

    private static void verifyCalculatorRelationships(
            CodeGraph graph) {

        verifyMainExists(graph);

        verifyRelationship(
                graph,
                "FUNCTION:main()",
                "METHOD:Calculator::add(int,int)",
                "CALLS"
        );

        verifyRelationship(
                graph,
                "FUNCTION:main()",
                "METHOD:Calculator::add(double,double)",
                "CALLS"
        );

        verifyRelationship(
                graph,
                "FUNCTION:main()",
                "FUNCTION:calculate()",
                "CALLS"
        );

        verifyRelationship(
                graph,
                "METHOD:Calculator::add(int,int)",
                "FUNCTION:helper()",
                "CALLS"
        );

        // -----------------------------------------------------
        // Verify inheritance
        // -----------------------------------------------------

        verifyRelationship(
                graph,
                "CLASS:Car",
                "CLASS:Vehicle",
                "INHERITS"
        );

        verifyReachability(
                graph,
                "FUNCTION:main()",
                "FUNCTION:helper()",
                "CALLS"
        );

        verifyPath(
                graph,
                "FUNCTION:main()",
                "FUNCTION:helper()",
                "CALLS"
        );
    }


    // =========================================================
    // Verify main exists
    // =========================================================

    private static void verifyMainExists(
            CodeGraph graph) {

        CodeEntity main =
                graph.findEntityById(
                        "FUNCTION:main()"
                );

        if (main == null) {

            throw new RuntimeException(
                    "VERIFICATION FAILED: main() does not exist"
            );
        }

        System.out.println(
                "PASS: main() exists"
        );
    }


    // =========================================================
    // Verify direct relationship
    // =========================================================

    private static void verifyRelationship(
            CodeGraph graph,
            String sourceId,
            String targetId,
            String relationshipType) {

        List<Relationship> relationships =
                graph.findRelationshipsFrom(
                        sourceId
                );

        for (Relationship relationship :
                relationships) {

            if (relationship.targetId.equals(targetId)
                    && relationship.type.equals(
                    relationshipType)) {

                System.out.println(
                        "PASS: "
                                + sourceId
                                + " --"
                                + relationshipType
                                + "--> "
                                + targetId
                );

                return;
            }
        }

        throw new RuntimeException(
                "VERIFICATION FAILED: "
                        + sourceId
                        + " --"
                        + relationshipType
                        + "--> "
                        + targetId
                        + " does not exist"
        );
    }


    // =========================================================
    // Verify reachability
    // =========================================================

    private static void verifyReachability(
            CodeGraph graph,
            String sourceId,
            String targetId,
            String relationshipType) {

        List<CodeEntity> reachable =
                graph.findReachableEntities(
                        sourceId,
                        relationshipType
                );

        for (CodeEntity entity :
                reachable) {

            if (entity.id.equals(targetId)) {

                System.out.println(
                        "PASS: "
                                + targetId
                                + " is reachable from "
                                + sourceId
                );

                return;
            }
        }

        throw new RuntimeException(
                "VERIFICATION FAILED: "
                        + targetId
                        + " is not reachable from "
                        + sourceId
        );
    }


    // =========================================================
    // Verify path
    // =========================================================

    private static void verifyPath(
            CodeGraph graph,
            String sourceId,
            String targetId,
            String relationshipType) {

        GraphPath path =
                graph.findPath(
                        sourceId,
                        targetId,
                        relationshipType
                );

        if (path == null) {

            throw new RuntimeException(
                    "VERIFICATION FAILED: "
                            + "No path exists from "
                            + sourceId
                            + " to "
                            + targetId
            );
        }

        System.out.println(
                "PASS: path exists from "
                        + sourceId
                        + " to "
                        + targetId
        );

        System.out.println(
                "      Path: "
                        + formatPath(path)
        );
    }


    // =========================================================
    // Format path for display
    // =========================================================

    private static String formatPath(
            GraphPath path) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < path.entities.size();
             i++) {

            CodeEntity entity =
                    path.entities.get(i);

            result.append(
                    entity.qualifiedName
            );

            if (i < path.relationships.size()) {

                Relationship relationship =
                        path.relationships.get(i);

                result.append(
                        " --"
                                + relationship.type
                                + "--> "
                );
            }
        }

        return result.toString();
    }
}