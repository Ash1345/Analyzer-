import java.util.ArrayList;
import java.util.List;

public class ConstructionAnalyzer {

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
        // Identify current function/method
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
        // Constructor expression
        //
        // Example:
        //
        // Calculator calculator;
        //
        // Clang represents the construction as
        // CXXConstructExpr.
        // -----------------------------------------------------

        if ("CXXConstructExpr".equals(node.kind)
                && currentFunction != null) {

            String constructedType =
                    extractConstructedType(node);

            if (constructedType != null) {

                CodeEntity targetClass =
                        findClassByType(
                                constructedType,
                                entityRegistry
                        );

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

    // =========================================================
    // Extract constructed type from CXXConstructExpr
    // =========================================================

    private static String extractConstructedType(
            AstNode node) {

        if (node.type == null) {
            return null;
        }

        Object qualType =
                node.type.get(
                        "qualType"
                );

        if (qualType == null) {
            return null;
        }

        String type =
                qualType.toString().trim();

        if (type.isEmpty()) {
            return null;
        }

        return type;
    }

    // =========================================================
    // Find CLASS entity matching constructed type
    // =========================================================

    private static CodeEntity findClassByType(
            String type,
            EntityRegistry entityRegistry) {

        if (type == null
                || entityRegistry == null) {

            return null;
        }

        String normalizedType =
                type
                        .replace("*", "")
                        .replace("&", "")
                        .trim();

        List<CodeEntity> entities =
                entityRegistry.getAll();

        for (CodeEntity entity :
                entities) {

            if (!"CLASS".equals(entity.kind)) {
                continue;
            }

            if (entity.name == null) {
                continue;
            }

            if (entity.name.equals(
                    normalizedType)) {

                return entity;
            }
        }

        return null;
    }

    // =========================================================
    // Create relationship
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

        RelationshipLocation.attach(
                relationship,
                node,
                locationResolver
        );

        return relationship;
    }
}