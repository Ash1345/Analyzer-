import java.util.ArrayList;
import java.util.List;

public class UsesAnalyzer {

    public static List<Relationship> analyze(
            AstNode root,
            AstIndex index) {

        List<Relationship> relationships =
                new ArrayList<>();

        analyzeNode(
                root,
                index,
                relationships,
                null
        );

        return relationships;
    }


    private static void analyzeNode(
            AstNode node,
            AstIndex index,
            List<Relationship> relationships,
            CodeEntity currentFunction) {

        if (node == null) {
            return;
        }


        // =====================================================
        // Track current function
        // =====================================================

        if (("FunctionDecl".equals(node.kind)
                || "CXXMethodDecl".equals(node.kind))
                && node.name != null) {

            CodeEntity entity =
                    index.resolveEntity(node.id);

            if (entity != null) {
                currentFunction = entity;
            }
        }


        // =====================================================
        // Detect variable/type usage
        // =====================================================

        if ("VarDecl".equals(node.kind)
                && currentFunction != null
                && node.type != null) {

            Object qualType =
                    node.type.get("qualType");

            if (qualType != null) {

                String variableType =
                        qualType.toString()
                                .replace("*", "")
                                .replace("&", "")
                                .trim();

                CodeEntity targetClass = null;

                for (CodeEntity entity :
                        index.getAllEntities()) {

                    if (!"CLASS".equals(entity.kind)) {
                        continue;
                    }

                    if (entity.name.equals(
                            variableType)) {

                        targetClass = entity;
                        break;
                    }
                }


                if (targetClass != null) {

                    Relationship relationship =
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    targetClass.id,
                                    targetClass.qualifiedName,
                                    "USES"
                            );

                    /*
                     * No duplicate checking here.
                     *
                     * RelationshipRegistry in GraphBuilder
                     * is responsible for deduplication.
                     */
                    relationships.add(
                            relationship
                    );
                }
            }
        }


        // =====================================================
        // Analyze children
        // =====================================================

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                analyzeNode(
                        child,
                        index,
                        relationships,
                        currentFunction
                );
            }
        }
    }
}