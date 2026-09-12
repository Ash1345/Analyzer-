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

        // Track current function
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

                    relationships.add(
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    variable.id,
                                    variable.qualifiedName,
                                    "USES_VARIABLE"
                            )
                    );
                }
            }
        }

        // Analyze children
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