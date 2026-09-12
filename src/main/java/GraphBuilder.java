import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {

    public static CodeGraph build(AstNode root) {

        // Build AST index
        AstIndex index = new AstIndex();
        index.build(root);

        // Extract entities
        List<CodeEntity> entities =
                EntityAnalyzer.analyze(root);

        // Add entities to index
        for (CodeEntity entity : entities) {
            index.addEntity(entity);
        }

        // Collect all relationships
        List<Relationship> relationships =
                new ArrayList<>();

        // CALLS
        relationships.addAll(
                CallAnalyzer.analyze(root, index)
        );

        // CONTAINS
        relationships.addAll(
                ContainsAnalyzer.analyze(entities)
        );

        // CONSTRUCTS
        relationships.addAll(
                ConstructionAnalyzer.analyze(root, index)
        );

        // USES
        relationships.addAll(
                UsesAnalyzer.analyze(root, index)
        );

        relationships.addAll(
                VariableUseAnalyzer.analyze(root, index)
        );

        // Create graph
        return new CodeGraph(
                entities,
                relationships
        );
    }
}