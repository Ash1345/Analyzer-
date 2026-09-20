import java.util.ArrayList;
import java.util.List;

public class ConstructionAnalyzer {

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
        // Detect object construction
        // =====================================================

        if ("CXXConstructExpr".equals(node.kind)
                && currentFunction != null) {

            String constructedType = null;

            if (node.type != null) {

                Object qualType =
                        node.type.get("qualType");

                if (qualType != null) {

                    constructedType =
                            qualType.toString();
                }
            }


            // =================================================
            // Find the corresponding class entity
            // =================================================

            if (constructedType != null) {

                CodeEntity targetClass = null;

                for (CodeEntity entity :
                        index.getAllEntities()) {

                    if (!"CLASS".equals(entity.kind)) {
                        continue;
                    }

                    if (entity.name.equals(
                            constructedType)) {

                        targetClass = entity;
                        break;
                    }
                }


                // =============================================
                // Create CONSTRUCTS relationship
                // =============================================

                if (targetClass != null) {

                    Relationship relationship =
                            createRelationship(
                                    currentFunction,
                                    targetClass,
                                    "CONSTRUCTS",
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