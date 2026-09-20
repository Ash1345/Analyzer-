import java.util.ArrayList;
import java.util.List;

public class VariableUseAnalyzer {

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
        // Variable reference
        // =====================================================

        if ("DeclRefExpr".equals(node.kind)
                && currentFunction != null
                && node.referencedDecl != null) {

            Object referencedIdObject =
                    node.referencedDecl.get("id");

            if (referencedIdObject != null) {

                String referencedId =
                        referencedIdObject.toString();

                CodeEntity variable =
                        index.resolveEntity(
                                referencedId
                        );

                if (variable != null
                        && "VARIABLE".equals(
                        variable.kind)) {

                    Relationship relationship =
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    variable.id,
                                    variable.qualifiedName,
                                    "USES_VARIABLE"
                            );

                    /*
                     * Do NOT perform duplicate checking here.
                     *
                     * RelationshipRegistry in GraphBuilder
                     * is now responsible for deduplication.
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