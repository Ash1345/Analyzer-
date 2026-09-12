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

        return new CodeGraph(
                entities,
                relationships
        );
    }
}