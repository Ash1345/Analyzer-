public class TranslationUnit {

    private final SourceFile sourceFile;
    private final AstNode ast;

    public TranslationUnit(
            SourceFile sourceFile,
            AstNode ast) {

        if (sourceFile == null) {
            throw new IllegalArgumentException(
                    "Source file cannot be null"
            );
        }

        if (ast == null) {
            throw new IllegalArgumentException(
                    "AST cannot be null"
            );
        }

        this.sourceFile = sourceFile;
        this.ast = ast;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public AstNode getAst() {
        return ast;
    }
}