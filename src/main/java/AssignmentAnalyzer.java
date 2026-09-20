import java.util.ArrayList;
import java.util.List;

public class AssignmentAnalyzer {

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
        // Assignment
        // =====================================================

        if ("BinaryOperator".equals(node.kind)
                && "=".equals(node.opcode)
                && currentFunction != null
                && node.inner != null
                && !node.inner.isEmpty()) {

            /*
             * The first child of an assignment is normally
             * the left-hand side.
             *
             * Example:
             *
             * result = result + 5;
             *
             * BinaryOperator (=)
             *     DeclRefExpr result
             *     BinaryOperator (+)
             */

            AstNode leftHandSide =
                    node.inner.get(0);

            CodeEntity variable =
                    findReferencedVariable(
                            leftHandSide,
                            index
                    );

            if (variable != null) {

                Relationship relationship =
                        new Relationship(
                                currentFunction.id,
                                currentFunction.qualifiedName,
                                variable.id,
                                variable.qualifiedName,
                                "WRITES"
                        );

                /*
                 * Do not perform duplicate checking here.
                 *
                 * RelationshipRegistry in GraphBuilder
                 * handles relationship deduplication.
                 */
                relationships.add(
                        relationship
                );
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


    private static CodeEntity findReferencedVariable(
            AstNode node,
            AstIndex index) {

        if (node == null) {
            return null;
        }


        // Direct variable reference
        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object referencedIdObject =
                    node.referencedDecl.get("id");

            if (referencedIdObject != null) {

                String referencedId =
                        referencedIdObject.toString();

                CodeEntity entity =
                        index.resolveEntity(
                                referencedId
                        );

                if (entity != null
                        && "VARIABLE".equals(
                        entity.kind)) {

                    return entity;
                }
            }
        }


        // Search children recursively
        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                CodeEntity result =
                        findReferencedVariable(
                                child,
                                index
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}