import java.util.ArrayList;
import java.util.List;

public class Relationship {

    // =========================================================
    // Entity identities
    // =========================================================

    public String sourceId;
    public String targetId;


    // =========================================================
    // Human-readable names
    // =========================================================

    public String source;
    public String target;


    // =========================================================
    // Relationship semantics
    // =========================================================

    /*
     * Examples:
     *
     * CALLS
     * OBJECT_CALLS
     * CONTAINS
     * CONSTRUCTS
     * USES
     * USES_VARIABLE
     * TYPE_OF
     * PRODUCES
     * WRITES
     */
    public String type;


    // =========================================================
    // Evidence information
    // =========================================================

    // Evidence occurrences
    public List<RelationshipEvidence> evidence =
            new ArrayList<>();
    /*
     * Source file where this relationship was discovered.
     */
    public String file;

    /*
     * Source-code line where the relationship was discovered.
     */
    public Integer line;

    /*
     * Source-code column where the relationship was discovered.
     */
    public Integer column;


    // =========================================================
    // Existing constructor
    // =========================================================

    public Relationship(
            String source,
            String target,
            String type) {

        this.source = source;
        this.target = target;
        this.type = type;
    }


    // =========================================================
    // Existing constructor with IDs
    // =========================================================

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


    // =========================================================
    // Evidence constructor
    // =========================================================

    public Relationship(
            String sourceId,
            String source,
            String targetId,
            String target,
            String type,
            String file,
            Integer line,
            Integer column) {

        this.sourceId = sourceId;
        this.source = source;
        this.targetId = targetId;
        this.target = target;
        this.type = type;

        this.file = file;
        this.line = line;
        this.column = column;
    }
}