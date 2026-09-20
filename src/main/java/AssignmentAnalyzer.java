import java.util.ArrayList;
import java.util.List;

public class AssignmentAnalyzer {

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
        // Detect assignment
        // =====================================================

        if ("BinaryOperator".equals(node.kind)
                && "=".equals(node.opcode)
                && currentFunction != null
                && node.inner != null
                && !node.inner.isEmpty()) {

            AstNode leftHandSide =
                    node.inner.get(0);

            CodeEntity variable =
                    findReferencedVariable(
                            leftHandSide,
                            index
                    );

            if (variable != null) {

                Relationship relationship =
                        createRelationship(
                                currentFunction,
                                variable,
                                "WRITES",
                                node,
                                locationResolver
                        );

                relationships.add(
                        relationship
                );
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

            Object referencedIdObject =
                    node.referencedDecl.get("id");

            if (referencedIdObject != null) {

                String referencedId =
                        referencedIdObject.toString();

                CodeEntity entity =
                        index.resolveEntity(
                                referencedId
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