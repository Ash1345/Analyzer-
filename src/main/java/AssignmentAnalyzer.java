import java.util.ArrayList;
import java.util.List;

public class AssignmentAnalyzer {

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

        // Track the function we are currently inside.
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.resolveEntity(node.id);
        }

        // Assignment: lhs = rhs
        if ("BinaryOperator".equals(node.kind)
                && "=".equals(getOpcode(node))
                && currentFunction != null
                && node.inner != null
                && !node.inner.isEmpty()) {

            AstNode leftSide =
                    node.inner.get(0);

            CodeEntity targetVariable =
                    findReferencedVariable(
                            leftSide,
                            index
                    );

            if (targetVariable != null) {

                Relationship relationship =
                        new Relationship(
                                currentFunction.id,
                                currentFunction.qualifiedName,
                                targetVariable.id,
                                targetVariable.qualifiedName,
                                "WRITES"
                        );

                boolean alreadyExists = false;

                for (Relationship existing :
                        relationships) {

                    if (existing.sourceId.equals(
                            relationship.sourceId)
                            && existing.targetId.equals(
                            relationship.targetId)
                            && existing.type.equals(
                            relationship.type)) {

                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {

                    relationships.add(
                            relationship
                    );
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

    private static String getOpcode(
            AstNode node) {

        return node.opcode;
    }

    private static CodeEntity findReferencedVariable(
            AstNode node,
            AstIndex index) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object referencedId =
                    node.referencedDecl.get("id");

            if (referencedId != null) {

                CodeEntity entity =
                        index.resolveEntity(
                                referencedId.toString()
                        );

                if (entity != null
                        && "VARIABLE".equals(entity.kind)) {

                    return entity;
                }
            }
        }

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