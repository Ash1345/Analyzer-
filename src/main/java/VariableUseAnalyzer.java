import java.util.ArrayList;
import java.util.List;

public class VariableUseAnalyzer {

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


        // =====================================================
        // Track current function
        // =====================================================

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


        // =====================================================
        // Detect variable usage
        // =====================================================

        if ("DeclRefExpr".equals(node.kind)
                && currentFunction != null
                && node.referencedDecl != null) {

            Object referencedIdObject =
                    node.referencedDecl.get("id");

            if (referencedIdObject != null) {

                String referencedId =
                        referencedIdObject.toString();

                CodeEntity variable =
                        entityRegistry.findByAstId(
                                referencedId
                        );

                if (variable != null
                        && "VARIABLE".equals(
                        variable.kind)) {

                    Relationship relationship =
                            createRelationship(
                                    currentFunction,
                                    variable,
                                    "USES_VARIABLE",
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
                        entityRegistry,
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

        RelationshipLocation.attach(
                relationship,
                node,
                locationResolver
        );

        return relationship;
    }
}