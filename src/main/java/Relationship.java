public class Relationship {

    public String source;
    public String target;
    public String type;

    public String sourceId;
    public String targetId;

    public Relationship(
            String source,
            String target,
            String type) {

        this.source = source;
        this.target = target;
        this.type = type;
    }

    public Relationship(
            String sourceId,
            String source,
            String targetId,
            String target,
            String type) {

        this.sourceId = sourceId;
        this.source = source;
        this.targetId = targetId;
        this.target = target;
        this.type = type;
    }
}