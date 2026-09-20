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

        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.resolveEntity(node.id);
        }

        if ("VarDecl".equals(node.kind)
                && currentFunction != null
                && node.type != null) {

            Object qualType =
                    node.type.get("qualType");

            if (qualType != null) {

                String type =
                        qualType.toString();

                type = type
                        .replace("*", "")
                        .replace("&", "")
                        .trim();

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

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

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