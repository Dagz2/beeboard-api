package org.note.domain.model;

/**
 * Enum representing mandatory classification categories for Notes.
 *
 * <p>Every Note MUST have exactly one Category, which determines the note's
 * primary purpose and type. Categories are predefined by the system and provide
 * structured organization for knowledge management.
 *
 * <p><strong>Category vs Tag distinction:</strong>
 * <ul>
 *   <li><strong>Category:</strong> WHAT TYPE of note is this? (mandatory, single, predefined)</li>
 *   <li><strong>Tag:</strong> WHAT TOPICS does it cover? (optional, multiple, user-defined)</li>
 * </ul>
 *
 * <p>Available categories:
 * <ul>
 *   <li>BUG: Issue reports and known problems</li>
 *   <li>TIP: Useful shortcuts and productivity tips</li>
 *   <li>DOCUMENTATION: Explanations and technical documentation</li>
 *   <li>QUESTION: Questions seeking answers and discussions</li>
 *   <li>ANNOUNCEMENT: Team communications and updates</li>
 *   <li>RECOMMENDATION: Suggestions and resource recommendations</li>
 *   <li>BUSINESS_RULE: Business logic and rule documentation</li>
 *   <li>CODE_SNIPPET: Reusable code examples and patterns</li>
 * </ul>
 */
public enum NoteCategory {

    /**
     * Bug report or known issue.
     * Example: "React state not updating in useEffect"
     */
    BUG,

    /**
     * Useful tip or shortcut.
     * Example: "VSCode shortcut: Ctrl+Shift+P"
     */
    TIP,

    /**
     * Documentation or explanation.
     * Example: "How our authentication flow works"
     */
    DOCUMENTATION,

    /**
     * Question seeking answers.
     * Example: "How to optimize this SQL query?"
     */
    QUESTION,

    /**
     * Team announcement.
     * Example: "React 19 is now available"
     */
    ANNOUNCEMENT,

    /**
     * Personal recommendation.
     * Example: "Great article on clean architecture"
     */
    RECOMMENDATION,

    /**
     * Business rule documentation.
     * Example: "Discount calculation formula"
     */
    BUSINESS_RULE,

    /**
     * Reusable code example.
     * Example: "Generic repository pattern in Java"
     */
    CODE_SNIPPET;

    /**
     * Checks if this category typically expects solutions or answers.
     *
     * @return true if category is QUESTION or BUG, false otherwise
     */
    public boolean expectsSolutions() {
        return this == QUESTION || this == BUG;
    }

    /**
     * Checks if this category represents knowledge documentation.
     *
     * @return true if category is DOCUMENTATION, BUSINESS_RULE, or CODE_SNIPPET
     */
    public boolean isKnowledgeBase() {
        return this == DOCUMENTATION || this == BUSINESS_RULE || this == CODE_SNIPPET;
    }

    /**
     * Checks if this category represents collaborative content.
     *
     * @return true if category is QUESTION, ANNOUNCEMENT, or RECOMMENDATION
     */
    public boolean isCollaborative() {
        return this == QUESTION || this == ANNOUNCEMENT || this == RECOMMENDATION;
    }
}
