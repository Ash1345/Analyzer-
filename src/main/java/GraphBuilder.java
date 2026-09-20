import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {

    public static CodeGraph build(AstNode root) {

        AstIndex index = new AstIndex();

        index.build(root);

        String sourceFile =
                "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp";

        List<CodeEntity> entities =
                EntityAnalyzer.analyze(
                        root,
                        sourceFile
                );

        for (CodeEntity entity : entities) {

            CodeEntity registeredEntity =
                    index.findOrCreateEntity(
                            entity
                    );

            /*
             * If the entity was new, it is already
             * registered inside the index.
             *
             * This keeps the existing behavior.
             */
        }


        RelationshipRegistry registry =
                new RelationshipRegistry();


        // =====================================================
        // Create source-location resolver
        // =====================================================

        SourceLocationResolver locationResolver;

        try {

            locationResolver =
                    new SourceLocationResolver(
                            sourceFile
                    );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to create source location resolver: "
                            + sourceFile,
                    e
            );
        }


        // =====================================================
        // Analyze relationships
        // =====================================================

        addRelationships(
                registry,
                CallAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );

        addRelationships(
                registry,
                ContainsAnalyzer.analyze(
                        entities
                )
        );

        addRelationships(
                registry,
                ConstructionAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );

        addRelationships(
                registry,
                UsesAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );

        addRelationships(
                registry,
                VariableUseAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );

        addRelationships(
                registry,
                TypeRelationshipAnalyzer.analyze(
                        entities
                )
        );

        addRelationships(
                registry,
                DataFlowAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );

        addRelationships(
                registry,
                AssignmentAnalyzer.analyze(
                        root,
                        index,
                        locationResolver
                )
        );


        return new CodeGraph(
                entities,
                registry.getRelationships()
        );
    }


    public static CodeGraph build(
            List<AstNode> roots,
            List<String> sourceFiles) {

        AstIndex index =
                new AstIndex();


        // =====================================================
        // First: index every translation unit
        // =====================================================

        for (AstNode root : roots) {

            addToIndex(
                    root,
                    index
            );
        }


        // =====================================================
        // Collect entities from every translation unit
        // =====================================================

        List<CodeEntity> entities =
                new ArrayList<>();

        for (int i = 0;
             i < roots.size();
             i++) {

            AstNode root =
                    roots.get(i);

            String sourceFile =
                    sourceFiles.get(i);


            List<CodeEntity> rootEntities =
                    EntityAnalyzer.analyze(
                            root,
                            sourceFile
                    );


            for (CodeEntity entity :
                    rootEntities) {

                CodeEntity registeredEntity =
                        index.findOrCreateEntity(
                                entity
                        );


                if (registeredEntity == entity) {

                    entities.add(
                            registeredEntity
                    );
                }
            }
        }


        // =====================================================
        // Central relationship registry
        // =====================================================

        RelationshipRegistry registry =
                new RelationshipRegistry();


        // =====================================================
        // Analyze every translation unit
        // =====================================================

        for (int i = 0;
             i < roots.size();
             i++) {

            AstNode root =
                    roots.get(i);

            String sourceFile =
                    sourceFiles.get(i);


            // -------------------------------------------------
            // Create resolver for this translation unit
            // -------------------------------------------------

            SourceLocationResolver locationResolver;

            try {

                locationResolver =
                        new SourceLocationResolver(
                                sourceFile
                        );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to create source location resolver: "
                                + sourceFile,
                        e
                );
            }


            // -------------------------------------------------
            // Call relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    CallAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );


            // -------------------------------------------------
            // Construction relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    ConstructionAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );


            // -------------------------------------------------
            // Uses relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    UsesAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );


            // -------------------------------------------------
            // Variable usage relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    VariableUseAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );


            // -------------------------------------------------
            // Data flow relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    DataFlowAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );


            // -------------------------------------------------
            // Assignment relationships
            // -------------------------------------------------

            addRelationships(
                    registry,
                    AssignmentAnalyzer.analyze(
                            root,
                            index,
                            locationResolver
                    )
            );
        }


        // =====================================================
        // Analyze complete entity collection
        // =====================================================

        addRelationships(
                registry,
                ContainsAnalyzer.analyze(
                        entities
                )
        );


        addRelationships(
                registry,
                TypeRelationshipAnalyzer.analyze(
                        entities
                )
        );


        // =====================================================
        // Build final graph
        // =====================================================

        return new CodeGraph(
                entities,
                registry.getRelationships()
        );
    }


    // =========================================================
    // Add relationships to central registry
    // =========================================================

    private static void addRelationships(
            RelationshipRegistry registry,
            List<Relationship> relationships) {

        if (relationships == null) {
            return;
        }

        for (Relationship relationship :
                relationships) {

            registry.add(
                    relationship
            );
        }
    }


    // =========================================================
    // AST indexing helper
    // =========================================================

    public static void addToIndex(
            AstNode root,
            AstIndex index) {

        if (root == null
                || index == null) {

            return;
        }

        index.build(root);
    }
}