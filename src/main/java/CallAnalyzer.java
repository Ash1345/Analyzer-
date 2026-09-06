public class CallAnalyzer {

    public static void analyze(AstNode node, AstIndex index) {

        analyze(node, index, null);
    }

    private static void analyze(
            AstNode node,
            AstIndex index,
            String currentFunction) {

        if (node == null) {
            return;
        }

        // Ignore implicit declarations
        if (Boolean.TRUE.equals(node.isImplicit)) {
            return;
        }

        // Remember current function
        if ("FunctionDecl".equals(node.kind)
                && node.name != null) {

            currentFunction = node.name;
        }

        // Detect method call
        if ("CXXMemberCallExpr".equals(node.kind)) {

            if (node.inner != null) {

                for (AstNode child : node.inner) {

                    if ("MemberExpr".equals(child.kind)) {

                        AstNode target =
                                index.findById(
                                        child.referencedMemberDecl
                                );

                        System.out.println(
                                "Caller: " + currentFunction
                        );

                        System.out.println(
                                "Called method: " + child.name
                        );

                        if (target != null) {

                            System.out.println(
                                    "Target AST node: "
                                            + target.kind
                            );

                            System.out.println(
                                    "Target declaration: "
                                            + target.name
                            );

                        } else {

                            System.out.println(
                                    "Target declaration not found"
                            );
                        }
                    }
                }
            }
        }

        // Continue through children
        if (node.inner != null) {

            for (AstNode child : node.inner) {

                analyze(
                        child,
                        index,
                        currentFunction
                );
            }
        }
    }
}