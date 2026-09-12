import java.util.ArrayList;
import java.util.List;

public class VariableUseAnalyzer {

    public static List<Relationship> analyze(
            AstNode node,
            AstIndex index) {

        List<Relationship> relationships =
                new ArrayList<>();

        analyze(
                node,
                index,
                null,
                null,
                relationships
        );

        return relationships;
    }

    private static void analyze(
            AstNode node,
            AstIndex index,
            AstNode parent,
            CodeEntity currentFunction,
            List<Relationship> relationships) {

        if (node == null) {
            return;
        }

        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        // Track the function we are currently inside
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.findEntityById(node.id);
        }

        // Variable reference
        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null
                && currentFunction != null) {

            Object referencedId =
                    node.referencedDecl.get("id");

            if (referencedId != null) {

                CodeEntity variable =
                        index.findEntityById(
                                referencedId.toString()
                        );

                if (variable != null
                        && "VARIABLE".equals(variable.kind)) {

                    // If this DeclRefExpr is the left side
                    // of an assignment, AssignmentAnalyzer
                    // already handles it as WRITES.
                    if (!isWriteReference(node, parent)) {

                        relationships.add(
                                new Relationship(
                                        currentFunction.id,
                                        currentFunction.qualifiedName,
                                        variable.id,
                                        variable.qualifiedName,
                                        "READS"
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
                        node,
                        currentFunction,
                        relationships
                );
            }
        }
    }

    private static boolean isWriteReference(
            AstNode node,
            AstNode parent) {

        if (node == null || parent == null) {
            return false;
        }

        if ("BinaryOperator".equals(parent.kind)
                && "=".equals(parent.opcode)
                && parent.inner != null
                && !parent.inner.isEmpty()) {

            AstNode leftSide =
                    parent.inner.get(0);

            return leftSide == node;
        }

        return false;
    }
}