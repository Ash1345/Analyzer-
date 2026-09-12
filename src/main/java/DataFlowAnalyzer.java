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

        /*
         * Remember current function
         */
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction =
                    index.findEntityById(node.id);
        }

        /*
         * Detect variable declarations
         *
         * Example:
         *
         * int result = calculator.add(10, 20);
         *
         * AST:
         *
         * VarDecl
         *   CXXMemberCallExpr
         *
         *
         * Example:
         *
         * int value = calculate();
         *
         * AST:
         *
         * VarDecl
         *   CallExpr
         */
        if ("VarDecl".equals(node.kind)
                && node.name != null
                && currentFunction != null
                && node.inner != null) {

            CodeEntity variable =
                    index.findEntityById(node.id);

            if (variable != null
                    && "VARIABLE".equals(variable.kind)) {

                for (AstNode child : node.inner) {

                    /*
                     * Method call:
                     *
                     * calculator.add(...)
                     */
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

                    /*
                     * Free function call:
                     *
                     * calculate()
                     */
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

        /*
         * Continue recursively
         */
        if (node.inner != null) {

            for (AstNode child : node.inner) {

                analyze(
                        child,
                        index,
                        currentFunction,
                        relationships
                );
            }
        }
    }

    /*
     * Resolve:
     *
     * CXXMemberCallExpr
     *   MemberExpr : add
     *
     * MemberExpr contains:
     *
     * referencedMemberDecl
     */
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

                System.out.println(
                        "DEBUG METHOD REF: "
                                + child.referencedMemberDecl
                );

                String referencedId =
                        child.referencedMemberDecl;

                if (referencedId != null) {

                    CodeEntity entity =
                            index.findEntityById(
                                    referencedId
                            );

                    System.out.println(
                            "DEBUG METHOD TARGET: "
                                    + (entity == null
                                    ? "NULL"
                                    : entity.qualifiedName)
                    );

                    return entity;
                }
            }
        }

        return null;
    }

    /*
     * Resolve:
     *
     * CallExpr
     *   ImplicitCastExpr
     *     DeclRefExpr
     *
     * The DeclRefExpr contains referencedDecl.id.
     */
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

            System.out.println(
                    "DEBUG FUNCTION REF: "
                            + referencedId
            );

            if (referencedId != null) {

                CodeEntity entity =
                        index.findEntityById(
                                referencedId.toString()
                        );

                System.out.println(
                        "DEBUG FUNCTION TARGET: "
                                + (entity == null
                                ? "NULL"
                                : entity.qualifiedName)
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