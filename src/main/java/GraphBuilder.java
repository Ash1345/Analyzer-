import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {

    public static CodeGraph build(AstNode root) {

        AstIndex index = new AstIndex();

        index.build(root);

        List<CodeEntity> entities =
                EntityAnalyzer.analyze(
                        root,
                        "C:\\Users\\ACER\\IdeaProjects\\AnalyzerPP\\test-project\\main.cpp"
                );

        for (CodeEntity entity : entities) {
            index.addEntity(entity);
        }

        List<Relationship> relationships =
                new ArrayList<>();

        relationships.addAll(
                CallAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                ContainsAnalyzer.analyze(entities)
        );

        relationships.addAll(
                ConstructionAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                UsesAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                VariableUseAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                TypeRelationshipAnalyzer.analyze(entities)
        );

        relationships.addAll(
                DataFlowAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                AssignmentAnalyzer.analyze(root, index)
        );

        return new CodeGraph(
                entities,
                relationships
        );
    }


    public static CodeGraph build(
            List<AstNode> roots,
            List<String> sourceFiles) {

        AstIndex index = new AstIndex();

        // --------------------------------------------------
        // Phase 1: Index every translation unit
        // --------------------------------------------------

        for (AstNode root : roots) {

            addToIndex(
                    root,
                    index
            );
        }


        // --------------------------------------------------
        // Phase 2: Analyze and register entities
        // --------------------------------------------------

        List<CodeEntity> entities =
                new ArrayList<>();

        for (int i = 0; i < roots.size(); i++) {

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


        // --------------------------------------------------
        // Phase 3: Analyze relationships
        // --------------------------------------------------

        List<Relationship> relationships =
                new ArrayList<>();

        for (AstNode root : roots) {

            relationships.addAll(
                    CallAnalyzer.analyze(
                            root,
                            index
                    )
            );

            relationships.addAll(
                    ConstructionAnalyzer.analyze(
                            root,
                            index
                    )
            );

            relationships.addAll(
                    UsesAnalyzer.analyze(
                            root,
                            index
                    )
            );

            relationships.addAll(
                    VariableUseAnalyzer.analyze(
                            root,
                            index
                    )
            );

            relationships.addAll(
                    DataFlowAnalyzer.analyze(
                            root,
                            index
                    )
            );

            relationships.addAll(
                    AssignmentAnalyzer.analyze(
                            root,
                            index
                    )
            );
        }


        // --------------------------------------------------
        // Phase 4: Analyze relationships that operate
        // on the complete entity collection
        // --------------------------------------------------

        relationships.addAll(
                ContainsAnalyzer.analyze(
                        entities
                )
        );

        relationships.addAll(
                TypeRelationshipAnalyzer.analyze(
                        entities
                )
        );


        return new CodeGraph(
                entities,
                relationships
        );
    }


    public static void addToIndex(
            AstNode root,
            AstIndex index) {

        if (root == null || index == null) {
            return;
        }

        index.build(root);
    }
}