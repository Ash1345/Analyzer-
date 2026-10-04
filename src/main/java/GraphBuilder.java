import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GraphBuilder {

    public static CodeGraph build(
            AstNode root,
            SourceFile sourceFile) {

        EntityRegistry entityRegistry =
                new EntityRegistry();

        List<CodeEntity> analyzedEntities =
                EntityAnalyzer.analyze(
                        root,
                        sourceFile.getPath()
                );

        for (CodeEntity entity :
                analyzedEntities) {

            entityRegistry.add(
                    entity
            );
        }

        List<CodeEntity> entities =
                entityRegistry.getAll();

        RelationshipRegistry registry =
                new RelationshipRegistry();

        // =====================================================
        // Create source-location resolver
        // =====================================================

        SourceLocationResolver locationResolver;

        try {

            locationResolver =
                    new SourceLocationResolver(
                            sourceFile.getPath()
                    );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to create source location resolver: "
                            + sourceFile.getPath(),
                    e
            );
        }

        // =====================================================
        // Create AST-based analyzers
        // =====================================================

        List<GraphAnalyzer> analyzers =
                createAnalyzers();

        // =====================================================
        // Run all AST-based analyzers
        // =====================================================

        analyzeAst(
                root,
                entityRegistry,
                locationResolver,
                registry,
                analyzers
        );

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


    public static CodeGraph build(
            List<AstNode> roots,
            List<SourceFile> sourceFiles) {

        // =====================================================
        // Collect entities from every translation unit
        // =====================================================

        EntityRegistry entityRegistry =
                new EntityRegistry();

        for (int i = 0;
             i < roots.size();
             i++) {

            AstNode root =
                    roots.get(i);

            SourceFile sourceFile =
                    sourceFiles.get(i);

            List<CodeEntity> rootEntities =
                    EntityAnalyzer.analyze(
                            root,
                            sourceFile.getPath()
                    );

            for (CodeEntity entity :
                    rootEntities) {

                entityRegistry.add(
                        entity
                );
            }
        }

        List<CodeEntity> entities =
                entityRegistry.getAll();

        // =====================================================
        // Central relationship registry
        // =====================================================

        RelationshipRegistry registry =
                new RelationshipRegistry();

        // =====================================================
        // Create AST-based analyzers
        // =====================================================

        List<GraphAnalyzer> analyzers =
                createAnalyzers();

        // =====================================================
        // Analyze every translation unit
        // =====================================================

        for (int i = 0;
             i < roots.size();
             i++) {

            AstNode root =
                    roots.get(i);

            SourceFile sourceFile =
                    sourceFiles.get(i);

            // -------------------------------------------------
            // Create resolver for this translation unit
            // -------------------------------------------------

            SourceLocationResolver locationResolver;

            try {

                locationResolver =
                        new SourceLocationResolver(
                                sourceFile.getPath()
                        );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to create source location resolver: "
                                + sourceFile.getPath(),
                        e
                );
            }

            // -------------------------------------------------
            // Run all AST-based analyzers
            // -------------------------------------------------

            analyzeAst(
                    root,
                    entityRegistry,
                    locationResolver,
                    registry,
                    analyzers
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
    // Analyze AST using all graph analyzers
    // =========================================================

    private static void analyzeAst(
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver,
            RelationshipRegistry registry,
            List<GraphAnalyzer> analyzers) {

        for (GraphAnalyzer analyzer :
                analyzers) {

            addRelationships(
                    registry,
                    analyzer.analyze(
                            root,
                            entityRegistry,
                            locationResolver
                    )
            );
        }
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


    private static List<GraphAnalyzer> createAnalyzers() {

        List<GraphAnalyzer> analyzers =
                new ArrayList<>();

        analyzers.add(
                new CallAnalyzer()
        );

        analyzers.add(
                new ConstructionAnalyzer()
        );

        analyzers.add(
                new UsesAnalyzer()
        );

        analyzers.add(
                new VariableUseAnalyzer()
        );

        analyzers.add(
                new DataFlowAnalyzer()
        );

        analyzers.add(
                new AssignmentAnalyzer()
        );

        return analyzers;
    }
}