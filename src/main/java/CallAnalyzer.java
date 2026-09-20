import java.util.ArrayList;
import java.util.List;

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

        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.resolveEntity(node.id);
        }

        if ("CXXMemberCallExpr".equals(node.kind)) {

            if (node.inner != null) {

                for (AstNode child : node.inner) {

                    if ("MemberExpr".equals(child.kind)) {

                        String referencedId =
                                child.referencedMemberDecl;

                        CodeEntity targetEntity =
                                index.resolveEntity(
                                        referencedId
                                );

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

                            if (child.inner != null) {

                                for (AstNode objectNode :
                                        child.inner) {

                                    if ("DeclRefExpr".equals(
                                            objectNode.kind)
                                            && objectNode.referencedDecl != null) {

                                        Object objectId =
                                                objectNode.referencedDecl
                                                        .get("id");

                                        if (objectId != null) {

                                            CodeEntity objectEntity =
                                                    index.resolveEntity(
                                                            objectId.toString()
                                                    );

                                            if (objectEntity != null
                                                    && "VARIABLE".equals(
                                                    objectEntity.kind)) {

                                                relations.add(
                                                        new Relationship(
                                                                objectEntity.id,
                                                                objectEntity.qualifiedName,
                                                                targetEntity.id,
                                                                targetEntity.qualifiedName,
                                                                "OBJECT_CALLS"
                                                        )
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null
                && currentFunction != null) {

            Object referencedId =
                    node.referencedDecl.get("id");

            if (referencedId != null) {

                CodeEntity targetEntity =
                        index.resolveEntity(
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