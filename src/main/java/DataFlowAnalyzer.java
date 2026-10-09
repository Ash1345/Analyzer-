
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DataFlowAnalyzer implements GraphAnalyzer {

    @Override
    public List<Relationship> analyze(
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

        // Track the current function or method.
        if (("FunctionDecl".equals(node.kind)
                || "CXXMethodDecl".equals(node.kind))
                && node.name != null) {

            CodeEntity entity =
                    entityRegistry.findByAstId(node.id);

            if (entity != null) {
                currentFunction = entity;
            }
        }

        // -----------------------------------------------------
        // 1. Variable initialization
        // -----------------------------------------------------
        // Examples:
        // int y = x;       -> x FLOWS_TO y
        // int a = x + 1;   -> x FLOWS_TO a
        // int c = x + a;   -> x FLOWS_TO c, a FLOWS_TO c
        // int result = foo(); -> foo PRODUCES result

        if ("VarDecl".equals(node.kind)
                && currentFunction != null
                && node.inner != null
                && !node.inner.isEmpty()) {

            CodeEntity targetVariable =
                    entityRegistry.findByAstId(node.id);

            if (targetVariable != null
                    && "VARIABLE".equals(targetVariable.kind)) {

                AstNode initializer =
                        node.inner.get(node.inner.size() - 1);

                // Collect all variable references in the initializer.
                for (CodeEntity sourceVariable :
                        findReferencedVariables(
                                initializer,
                                entityRegistry)) {

                    if (!sourceVariable.id.equals(targetVariable.id)) {
                        relationships.add(
                                createRelationship(
                                        sourceVariable,
                                        targetVariable,
                                        "FLOWS_TO",
                                        node,
                                        locationResolver
                                )
                        );
                    }
                }

                // Preserve existing function/method call tracking.
                CodeEntity sourceEntity =
                        findCalledMethod(node, entityRegistry);

                if (sourceEntity == null) {
                    sourceEntity =
                            findCalledFunction(node, entityRegistry);
                }

                if (sourceEntity != null) {
                    relationships.add(
                            createRelationship(
                                    sourceEntity,
                                    targetVariable,
                                    "PRODUCES",
                                    node,
                                    locationResolver
                            )
                    );
                }
            }
        }

        // -----------------------------------------------------
        // 2. Assignment between existing variables
        // -----------------------------------------------------
        // Examples:
        // y = x;       -> x FLOWS_TO y
        // a = x + 1;   -> x FLOWS_TO a
        // c = x + a;   -> x FLOWS_TO c, a FLOWS_TO c

        if ("BinaryOperator".equals(node.kind)
                && "=".equals(node.opcode)
                && currentFunction != null
                && node.inner != null
                && node.inner.size() >= 2) {

            AstNode leftHandSide = node.inner.get(0);
            AstNode rightHandSide = node.inner.get(1);

            // The target must be a direct variable reference.
            CodeEntity targetVariable =
                    findDirectReferencedVariable(
                            leftHandSide,
                            entityRegistry
                    );

            if (targetVariable != null) {

                // Collect every variable used by the RHS expression.
                for (CodeEntity sourceVariable :
                        findReferencedVariables(
                                rightHandSide,
                                entityRegistry)) {

                    if (!sourceVariable.id.equals(targetVariable.id)) {
                        relationships.add(
                                createRelationship(
                                        sourceVariable,
                                        targetVariable,
                                        "FLOWS_TO",
                                        node,
                                        locationResolver
                                )
                        );
                    }
                }
            }
        }

        // Analyze children while preserving the current function.
        if (node.inner != null) {
            for (AstNode child : node.inner) {
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
    // Find a direct variable reference
    // =========================================================
    // Supports DeclRefExpr and transparent AST wrappers such as
    // ImplicitCastExpr and ParenExpr.
    //
    // Does not search arbitrary expression descendants, avoiding
    // false direct-copy edges for expressions such as x + 1 or foo(x).

    private static CodeEntity findDirectReferencedVariable(
            AstNode node,
            EntityRegistry entityRegistry) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)) {

            if (node.referencedDecl == null) {
                return null;
            }

            Object idObject =
                    node.referencedDecl.get("id");

            if (idObject == null) {
                return null;
            }

            CodeEntity entity =
                    entityRegistry.findByAstId(
                            idObject.toString()
                    );

            if (entity != null
                    && "VARIABLE".equals(entity.kind)) {
                return entity;
            }

            return null;
        }

        if (isTransparentExpression(node.kind)
                && node.inner != null
                && node.inner.size() == 1) {

            return findDirectReferencedVariable(
                    node.inner.get(0),
                    entityRegistry
            );
        }

        return null;
    }


    private static List<CodeEntity> findReferencedVariables(
            AstNode node,
            EntityRegistry entityRegistry) {

        Map<String, CodeEntity> variables = new LinkedHashMap<>();
        collectReferencedVariables(node, entityRegistry, variables);
        return new ArrayList<>(variables.values());
    }


    private static void collectReferencedVariables(
            AstNode node,
            EntityRegistry entityRegistry,
            Map<String, CodeEntity> variables) {

        if (node == null) {
            return;
        }

        // For a method call such as calculator.add(x, a),
        // skip the object receiver "calculator".
        if ("CXXMemberCallExpr".equals(node.kind)) {

            if (node.inner != null && node.inner.size() > 1) {

                // Child 0 represents the method expression/receiver.
                // Skip it and traverse the remaining argument expressions.
                for (int i = 1; i < node.inner.size(); i++) {
                    collectReferencedVariables(
                            node.inner.get(i),
                            entityRegistry,
                            variables
                    );
                }
            }

            return;
        }

        // Collect references to variables.
        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object idObject = node.referencedDecl.get("id");

            if (idObject != null) {
                CodeEntity entity =
                        entityRegistry.findByAstId(idObject.toString());

                if (entity != null && "VARIABLE".equals(entity.kind)) {
                    variables.putIfAbsent(entity.id, entity);
                }
            }
        }

        // Continue traversing the AST.
        if (node.inner != null) {
            for (AstNode child : node.inner) {
                collectReferencedVariables(
                        child,
                        entityRegistry,
                        variables
                );
            }
        }
    }



    private static boolean isTransparentExpression(String kind) {

        if (kind == null) {
            return false;
        }

        return switch (kind) {
            case "ImplicitCastExpr",
                 "ParenExpr",
                 "ExprWithCleanups",
                 "MaterializeTemporaryExpr",
                 "CXXBindTemporaryExpr",
                 "ConstantExpr",
                 "CStyleCastExpr",
                 "CXXStaticCastExpr",
                 "CXXFunctionalCastExpr",
                 "CXXReinterpretCastExpr",
                 "CXXConstCastExpr",
                 "CXXDynamicCastExpr" -> true;
            default -> false;
        };
    }

    // =========================================================
    // Find called method
    // =========================================================

    private static CodeEntity findCalledMethod(
            AstNode node,
            EntityRegistry entityRegistry) {

        if (node == null) {
            return null;
        }

        if ("CXXMemberCallExpr".equals(node.kind)) {

            AstNode memberExpr =
                    findNodeByKind(node, "MemberExpr");

            if (memberExpr != null
                    && memberExpr.referencedMemberDecl != null) {

                return entityRegistry.findByAstId(
                        memberExpr.referencedMemberDecl
                );
            }
        }

        if (node.inner != null) {
            for (AstNode child : node.inner) {

                CodeEntity result =
                        findCalledMethod(
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

    // =========================================================
    // Find called function
    // =========================================================

    private static CodeEntity findCalledFunction(
            AstNode node,
            EntityRegistry entityRegistry) {

        if (node == null) {
            return null;
        }

        if ("DeclRefExpr".equals(node.kind)
                && node.referencedDecl != null) {

            Object idObject =
                    node.referencedDecl.get("id");

            if (idObject != null) {

                CodeEntity entity =
                        entityRegistry.findByAstId(
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
            for (AstNode child : node.inner) {

                CodeEntity result =
                        findCalledFunction(
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
            for (AstNode child : node.inner) {

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

        RelationshipLocation.attach(
                relationship,
                node,
                locationResolver
        );

        return relationship;
    }
}
