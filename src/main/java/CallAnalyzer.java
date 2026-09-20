import java.util.ArrayList;
import java.util.List;

public class CallAnalyzer {

    public static List<Relationship> analyze(
            AstNode root,
            AstIndex index,
            SourceLocationResolver locationResolver) {

        List<Relationship> relationships =
                new ArrayList<>();

        analyzeNode(
                root,
                index,
                relationships,
                null,
                locationResolver
        );

        return relationships;
    }


    private static void analyzeNode(
            AstNode node,
            AstIndex index,
            List<Relationship> relationships,
            CodeEntity currentFunction,
            SourceLocationResolver locationResolver) {

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
        // C++ member call
        // =====================================================

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
                        index.resolveEntity(
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


                    // =================================================
                    // Detect object on which the method is called
                    // =================================================

                    CodeEntity object =
                            findReferencedVariable(
                                    node,
                                    index
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


        // =====================================================
        // Normal function call
        // =====================================================

        if ("CallExpr".equals(node.kind)
                && currentFunction != null) {

            AstNode referencedFunction =
                    findReferencedDecl(
                            node
                    );

            if (referencedFunction != null
                    && referencedFunction.id != null) {

                CodeEntity target =
                        index.resolveEntity(
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
                        currentFunction,
                        locationResolver
                );
            }
        }
    }


    // =========================================================
    // Create relationship with source location
    // =========================================================

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

        // =========================================================
        // Resolve source location from AST range.begin.offset
        // =========================================================

        if (node != null
                && node.range != null
                && locationResolver != null) {

            Object beginObject =
                    node.range.get("begin");

            if (beginObject instanceof java.util.Map) {

                java.util.Map<?, ?> begin =
                        (java.util.Map<?, ?>) beginObject;

                Object offsetObject =
                        begin.get("offset");

                if (offsetObject != null) {

                    int offset =
                            Integer.parseInt(
                                    offsetObject.toString()
                            );

                    SourceLocationResolver.Location location =
                            locationResolver.resolve(
                                    offset
                            );

                    relationship.file =
                            location.file;

                    relationship.line =
                            location.line;

                    relationship.column =
                            location.column;
                }
            }
        }

        return relationship;
    }


    // =========================================================
    // Find node by kind
    // =========================================================

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


    // =========================================================
    // Find referenced declaration
    // =========================================================

    private static AstNode findReferencedDecl(
            AstNode node) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object idObject =
                    node.referencedDecl.get("id");

            if (idObject != null) {

                AstNode result =
                        new AstNode();

                result.id =
                        idObject.toString();

                return result;
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


    // =========================================================
    // Find referenced variable
    // =========================================================

    private static CodeEntity findReferencedVariable(
            AstNode node,
            AstIndex index) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object idObject =
                    node.referencedDecl.get("id");

            if (idObject != null) {

                CodeEntity entity =
                        index.resolveEntity(
                                idObject.toString()
                        );

                if (entity != null
                        && "VARIABLE".equals(
                        entity.kind)) {

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