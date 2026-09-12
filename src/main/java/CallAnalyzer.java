import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CallAnalyzer {

    public static List<Relationship> analyze(
            AstNode node,
            AstIndex index) {

        List<Relationship> relations =
                new ArrayList<>();

        analyze(
                node,
                index,
                null,
                relations
        );

        return relations;
    }

    private static void analyze(
            AstNode node,
            AstIndex index,
            CodeEntity currentFunction,
            List<Relationship> relations) {

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

        // Detect method call
        if ("CXXMemberCallExpr".equals(node.kind)) {

            if (node.inner != null) {

                for (AstNode child : node.inner) {

                    if ("MemberExpr".equals(child.kind)) {

                        String referencedId =
                                child.referencedMemberDecl;

                        CodeEntity targetEntity =
                                index.findEntityById(referencedId);

                        if (targetEntity != null
                                && currentFunction != null) {

                            relations.add(
                                    new Relationship(
                                            currentFunction.id,
                                            currentFunction.qualifiedName,
                                            targetEntity.id,
                                            targetEntity.qualifiedName,
                                            "CALLS"
                                    )
                            );
                        }
                    }
                }
            }
        }

        // Detect free function call
        if ("CallExpr".equals(node.kind)) {

            if (node.inner != null) {

                for (AstNode child : node.inner) {

                    findReferencedFunction(
                            child,
                            index,
                            currentFunction,
                            relations
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
                        relations
                );
            }
        }
    }

    private static void findReferencedFunction(
            AstNode node,
            AstIndex index,
            CodeEntity currentFunction,
            List<Relationship> relations) {

        if (node == null) {
            return;
        }

        // We are looking for DeclRefExpr
        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null
                && currentFunction != null) {

            Object referencedId =
                    node.referencedDecl.get("id");

            if (referencedId != null) {

                CodeEntity targetEntity =
                        index.findEntityById(
                                referencedId.toString()
                        );

                if (targetEntity != null) {

                    relations.add(
                            new Relationship(
                                    currentFunction.id,
                                    currentFunction.qualifiedName,
                                    targetEntity.id,
                                    targetEntity.qualifiedName,
                                    "CALLS"
                            )
                    );
                }
            }

            return;
        }

        // Continue searching inside the CallExpr
        if (node.inner != null) {

            for (AstNode child : node.inner) {

                findReferencedFunction(
                        child,
                        index,
                        currentFunction,
                        relations
                );
            }
        }
    }
}