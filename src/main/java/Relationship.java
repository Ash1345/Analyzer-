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

    /*
     * A relationship can occur multiple times in the source code.
     *
     * Example:
     *
     * main --CALLS--> Calculator::add
     *
     * Evidence:
     *     main.cpp:25:18
     *     main.cpp:27:14
     */
    public List<RelationshipEvidence> evidence =
            new ArrayList<>();


    // =========================================================
    // Constructor
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
    // Constructor with entity IDs
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
}