import java.util.ArrayList;
import java.util.List;

public class ConstructionAnalyzer {

    public static List<Relationship> analyze(
            AstNode node,
            AstIndex index) {

        List<Relationship> relationships =
                new ArrayList<>();

        analyze(
                node,
                index,
                null,
                relationships
        );

        return relationships;
    }

    private static void analyze(
            AstNode node,
            AstIndex index,
            CodeEntity currentFunction,
            List<Relationship> relationships) {

        if (node == null) {
            return;
        }

        // Ignore implicit declarations
        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        // Remember current function
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.findEntityById(node.id);
        }

        // Detect object construction
        if ("CXXConstructExpr".equals(node.kind)
                && currentFunction != null
                && node.type != null) {

            Object qualType =
                    node.type.get("qualType");

            if (qualType != null) {

                String constructedType =
                        qualType.toString();

                CodeEntity targetEntity = null;

                for (CodeEntity entity :
                        findAllEntities(index)) {

                    if ("CLASS".equals(entity.kind)
                            && entity.name.equals(constructedType)) {

                        targetEntity = entity;
                        break;
                    }
                }

                if (targetEntity != null) {

                    relationships.add(
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    targetEntity.id,
                                    targetEntity.qualifiedName,
                                    "CONSTRUCTS"
                            )
                    );
                }
            }
        }

        // Continue through children
        if (node.inner != null) {

            for (AstNode child : node.inner) {

                analyze(
                        child,
                        index,
                        currentFunction,
                        relationships
                );
            }
        }
    }

    private static List<CodeEntity> findAllEntities(
            AstIndex index) {

        return index.getAllEntities();
    }
}