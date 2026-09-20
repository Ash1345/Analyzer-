import java.util.ArrayList;
import java.util.List;

public class DataFlowAnalyzer {

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
                && node.name != null
                && currentFunction != null
                && node.inner != null) {

            CodeEntity variable =
                    index.resolveEntity(node.id);

            if (variable != null
                    && "VARIABLE".equals(variable.kind)) {

                for (AstNode child : node.inner) {

                    if ("CXXMemberCallExpr".equals(
                            child.kind)) {

                        CodeEntity target =
                                findCalledMethod(
                                        child,
                                        index
                                );

                        if (target != null) {

                            relationships.add(
                                    new Relationship(
                                            target.id,
                                            target.qualifiedName,
                                            variable.id,
                                            variable.qualifiedName,
                                            "PRODUCES"
                                    )
                            );
                        }
                    }

                    if ("CallExpr".equals(
                            child.kind)) {

                        CodeEntity target =
                                findCalledFunction(
                                        child,
                                        index
                                );

                        if (target != null) {

                            relationships.add(
                                    new Relationship(
                                            target.id,
                                            target.qualifiedName,
                                            variable.id,
                                            variable.qualifiedName,
                                            "PRODUCES"
                                    )
                            );
                        }
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

    private static CodeEntity findCalledMethod(
            AstNode node,
            AstIndex index) {

        if (node == null
                || node.inner == null) {

            return null;
        }

        for (AstNode child : node.inner) {

            if ("MemberExpr".equals(
                    child.kind)) {

                String referencedId =
                        child.referencedMemberDecl;

                if (referencedId != null) {

                    return index.resolveEntity(
                            referencedId
                    );
                }
            }
        }

        return null;
    }

    private static CodeEntity findCalledFunction(
            AstNode node,
            AstIndex index) {

        if (node == null
                || node.inner == null) {

            return null;
        }

        for (AstNode child : node.inner) {

            CodeEntity result =
                    findReferencedFunction(
                            child,
                            index
                    );

            if (result != null) {

                return result;
            }
        }

        return null;
    }

    private static CodeEntity findReferencedFunction(
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
                        && ("FUNCTION".equals(
                        entity.kind)
                        || "METHOD".equals(
                        entity.kind))) {

                    return entity;
                }
            }
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                CodeEntity result =
                        findReferencedFunction(
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