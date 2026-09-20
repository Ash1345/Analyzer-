import java.util.ArrayList;
import java.util.List;

public class DataFlowAnalyzer {

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
        // Detect variable initialization from a call
        // =====================================================

        if ("VarDecl".equals(node.kind)
                && currentFunction != null
                && node.inner != null) {

            CodeEntity targetVariable =
                    index.resolveEntity(node.id);

            if (targetVariable != null
                    && "VARIABLE".equals(
                    targetVariable.kind)) {

                CodeEntity sourceEntity = null;

                // =================================================
                // Search initializer for a method/function call
                // =================================================

                sourceEntity =
                        findCalledMethod(
                                node,
                                index
                        );

                if (sourceEntity == null) {

                    sourceEntity =
                            findCalledFunction(
                                    node,
                                    index
                            );
                }


                // =================================================
                // Create PRODUCES relationship
                // =================================================

                if (sourceEntity != null) {

                    Relationship relationship =
                            createRelationship(
                                    sourceEntity,
                                    targetVariable,
                                    "PRODUCES",
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
    // Find called method
    // =========================================================

    private static CodeEntity findCalledMethod(
            AstNode node,
            AstIndex index) {

        if (node == null) {
            return null;
        }

        if ("CXXMemberCallExpr".equals(node.kind)) {

            AstNode memberExpr =
                    findNodeByKind(
                            node,
                            "MemberExpr"
                    );

            if (memberExpr != null
                    && memberExpr.referencedMemberDecl != null) {

                return index.resolveEntity(
                        memberExpr.referencedMemberDecl
                );
            }
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                CodeEntity result =
                        findCalledMethod(
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


    // =========================================================
    // Find called function
    // =========================================================

    private static CodeEntity findCalledFunction(
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
                        && ("FUNCTION".equals(entity.kind)
                        || "METHOD".equals(entity.kind))) {

                    return entity;
                }
            }
        }

        if (node.inner != null) {

            for (AstNode child :
                    node.inner) {

                CodeEntity result =
                        findCalledFunction(
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


        // =====================================================
        // Resolve location from range.begin.offset
        // =====================================================

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
}