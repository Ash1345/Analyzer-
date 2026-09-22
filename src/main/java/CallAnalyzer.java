import java.util.ArrayList;
import java.util.List;

public class CallAnalyzer {

    public static List<Relationship> analyze(
            AstNode root,
            EntityRegistry entityRegistry,
            SourceLocationResolver locationResolver) {

        List<Relationship> relationships =
                new ArrayList<>();

        analyzeNode(
                root,
                entityRegistry,
                relationships,
                null,
                locationResolver
        );

        return relationships;
    }

    private static void analyzeNode(
            AstNode node,
            EntityRegistry entityRegistry,
            List<Relationship> relationships,
            CodeEntity currentFunction,
            SourceLocationResolver locationResolver) {

        if (node == null) {
            return;
        }

        // -----------------------------------------------------
        // Detect current function or method
        // -----------------------------------------------------

        if (("FunctionDecl".equals(node.kind)
                || "CXXMethodDecl".equals(node.kind))
                && node.name != null) {

            CodeEntity entity =
                    entityRegistry.findByAstId(
                            node.id
                    );

            if (entity != null) {
                currentFunction = entity;
            }
        }

        // -----------------------------------------------------
        // Member function calls
        //
        // Example:
        // calculator.add(10, 20);
        // -----------------------------------------------------

        if ("CXXMemberCallExpr".equals(node.kind)
                && currentFunction != null) {

            AstNode memberExpr =
                    findNodeByKind(
                            node,
                            "MemberExpr"
                    );

            if (memberExpr != null
                    && memberExpr.referencedMemberDecl != null) {

                CodeEntity target =
                        entityRegistry.findByAstId(
                                memberExpr.referencedMemberDecl
                        );

                if (target != null) {

                    Relationship relationship =
                            createRelationship(
                                    currentFunction,
                                    target,
                                    "CALLS",
                                    node,
                                    locationResolver
                            );

                    relationships.add(
                            relationship
                    );

                    CodeEntity object =
                            findReferencedVariable(
                                    node,
                                    entityRegistry
                            );

                    if (object != null) {

                        Relationship objectRelationship =
                                createRelationship(
                                        object,
                                        target,
                                        "OBJECT_CALLS",
                                        node,
                                        locationResolver
                                );

                        relationships.add(
                                objectRelationship
                        );
                    }
                }
            }
        }

        // -----------------------------------------------------
        // Normal function calls
        //
        // Example:
        // calculate();
        // -----------------------------------------------------

        if ("CallExpr".equals(node.kind)
                && currentFunction != null) {

            AstNode referencedFunction =
                    findReferencedDecl(node);

            if (referencedFunction != null
                    && referencedFunction.id != null) {

                CodeEntity target =
                        entityRegistry.findByAstId(
                                referencedFunction.id
                        );

                if (target != null) {

                    Relationship relationship =
                            createRelationship(
                                    currentFunction,
                                    target,
                                    "CALLS",
                                    node,
                                    locationResolver
                            );

                    relationships.add(
                            relationship
                    );
                }
            }
        }

        // -----------------------------------------------------
        // Continue walking AST
        // -----------------------------------------------------

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                analyzeNode(
                        child,
                        entityRegistry,
                        relationships,
                        currentFunction,
                        locationResolver
                );
            }
        }
    }

    private static Relationship createRelationship(
            CodeEntity source,
            CodeEntity target,
            String type,
            AstNode node,
            SourceLocationResolver locationResolver) {

        Relationship relationship =
                new Relationship(
                        source.id,
                        source.qualifiedName,
                        target.id,
                        target.qualifiedName,
                        type
                );

        RelationshipLocation.attach(
                relationship,
                node,
                locationResolver
        );

        return relationship;
    }

    private static AstNode findNodeByKind(
            AstNode node,
            String kind) {

        if (node == null) {
            return null;
        }

        if (kind.equals(node.kind)) {
            return node;
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                AstNode result =
                        findNodeByKind(
                                child,
                                kind
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static AstNode findReferencedDecl(
            AstNode node) {

        if (node == null) {
            return null;
        }

        if (node.referencedDecl != null) {

            Object id =
                    node.referencedDecl.get(
                            "id"
                    );

            if (id != null) {

                AstNode referencedNode =
                        new AstNode();

                referencedNode.id =
                        id.toString();

                return referencedNode;
            }
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                AstNode result =
                        findReferencedDecl(
                                child
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static CodeEntity findReferencedVariable(
            AstNode node,
            EntityRegistry entityRegistry) {

        if (node == null) {
            return null;
        }

        if (node.referencedDecl != null) {

            Object kind =
                    node.referencedDecl.get(
                            "kind"
                    );

            Object id =
                    node.referencedDecl.get(
                            "id"
                    );

            if (kind != null
                    && id != null
                    && "VarDecl".equals(
                    kind.toString()
            )) {

                return entityRegistry.findByAstId(
                        id.toString()
                );
            }
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                CodeEntity result =
                        findReferencedVariable(
                                child,
                                entityRegistry
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}