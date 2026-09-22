public class RelationshipEvidence {

    public String file;
    public Integer line;
    public Integer column;

    public RelationshipEvidence() {
    }

    public RelationshipEvidence(
            String file,
            Integer line,
            Integer column) {

        this.file = file;
        this.line = line;
        this.column = column;
    }
}