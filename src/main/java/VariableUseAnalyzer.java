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

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null
                && currentFunction != null) {

            Object referencedId =
                    node.referencedDecl.get("id");

            if (referencedId != null) {

                CodeEntity variable =
                        index.resolveEntity(
                                referencedId.toString()
                        );

                if (variable != null
                        && "VARIABLE".equals(variable.kind)) {

                    Relationship relationship =
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    variable.id,
                                    variable.qualifiedName,
                                    "USES_VARIABLE"
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