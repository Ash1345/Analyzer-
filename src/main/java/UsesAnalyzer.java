import java.util.ArrayList;
import java.util.List;

public class UsesAnalyzer {

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

        // Detect variable declarations
        if ("VarDecl".equals(node.kind)
                && currentFunction != null
                && node.type != null) {

            Object qualType =
                    node.type.get("qualType");

            if (qualType != null) {

                String type =
                        qualType.toString();

                // Remove pointer/reference symbols
                type = type
                        .replace("*", "")
                        .replace("&", "")
                        .trim();

                // Find matching class
                for (CodeEntity entity :
                        index.getAllEntities()) {

                    if ("CLASS".equals(entity.kind)
                            && entity.name.equals(type)) {

                        relationships.add(
                                new Relationship(
                                        currentFunction.id,
                                        currentFunction.qualifiedName,
                                        entity.id,
                                        entity.qualifiedName,
                                        "USES"
                                )
                        );
                    }
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
}