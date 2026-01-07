# Ubiquitous Language Glossary
## Bounded Context: Note Management

**Domain:** Memory - Developer Collaboration Platform  
**Version:** 4.0 (Aligned with Domain Glossary + Full BC Autonomy)  
**Last Updated:** 2025-01-04

---

## 🎯 Context Overview

This Bounded Context handles the creation, sharing, classification, and discussion of **Notes** within the Memory platform. Notes are the primary means of knowledge sharing among developers - they can contain tips, technical information, business rules, formulas, or any information worth sharing. This is the **most anticipated feature** of the application.

### Design Principles

1. **Full BC Autonomy** - Uses its own Ubiquitous Language, translated from external contexts
2. **Alignment with Domain Glossary** - Respects the established domain vocabulary
3. **Clear Separation** - Category (mandatory, predefined) vs Tag (optional, flexible)
4. **Discussion-Centric** - Notes are meant to spark discussions and find solutions

---

## 🔗 Integration with Other Bounded Contexts

### Anti-Corruption Layer: NoteUserContext

BC Note **never uses external vocabulary directly**. All external concepts are translated to local vocabulary via `NoteUserContext`.

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ANTI-CORRUPTION LAYER                             │
│                                                                     │
│   EXTERNAL (from BC Access)          LOCAL (BC Note vocabulary)     │
│   ─────────────────────────          ─────────────────────────      │
│                                                                     │
│   ApplicationRole                    AccessLevel                    │
│   ├── ADMIN ──────────────────────▶ ├── SUPERVISOR                  │
│   ├── SUPERVISOR ─────────────────▶ ├── SUPERVISOR                  │
│   ├── MANAGER ────────────────────▶ ├── MODERATOR                   │
│   ├── MEMBER ─────────────────────▶ ├── COMMENTER                   │
│   └── GUEST ──────────────────────▶ └── VIEWER                      │
│                                                                     │
│   AuthType                           TrustScope                     │
│   ├── INTERNAL ───────────────────▶ ├── TEAM_MEMBER                 │
│   └── EXTERNAL ───────────────────▶ └── RESTRICTED_USER             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### NoteUserContext (Value Object)

| Field | Type | Description |
|-------|------|-------------|
| `userId` | UUID | Unique user identifier |
| `accessLevel` | AccessLevel | Translated access level for note operations |
| `trustScope` | TrustScope | Translated trust scope for visibility rules |

---

## 🔐 Access Levels (Local Vocabulary)

These are **BC Note's own concepts**, translated from external ApplicationRole.

| Access Level | Description | Capabilities |
|--------------|-------------|--------------|
| `SUPERVISOR` | Global oversight role | View all notes, moderate all, access archived, validate solutions |
| `MODERATOR` | Content management role | Moderate comments within owned projects |
| `COMMENTER` | Standard participant role | View per visibility, comment, propose solutions |
| `VIEWER` | Read-only role | View per visibility, cannot comment |

---

## 🌐 Trust Scopes (Local Vocabulary)

| Trust Scope | Description | Default Visibility |
|-------------|-------------|-------------------|
| `TEAM_MEMBER` | Trusted member of the organization | Can see SHARED notes by default |
| `RESTRICTED_USER` | Limited trust, temporary access | Only sees explicitly shared content |

---

## 📚 Core Domain Concepts

### Note

| Attribute | Description |
|-----------|-------------|
| **Definition** | A piece of content containing information to be shared on various topics. Can include keyboard shortcuts, business rules, calculation formulas, technology news, code snippets, or personal recommendations. |
| **French Equivalent** | Note |
| **Business Rule** | Notes are distinct from Tasks. A Task describes work to be done; a Note contains information to be shared and can spark discussions leading to solutions. |
| **Lifecycle** | Active → (Archived → Deleted) |

**Key Distinction from Task:**
> "We call **Tasks**: text describing work to be done, assigned to a developer, with varying importance and urgency.  
> We call **Notes**: text containing information to share - they are purely informative and can lead to discussions."

**Invariants:**
- A Note MUST have exactly one Author
- A Note MUST have exactly one Category (mandatory classification)
- A Note MUST have a Visibility type
- A Note CAN have zero or more Tags (flexible keywords)
- A Note CAN have zero or more Comments forming a Discussion
- A Note CAN be linked to zero or one Project (required for PROJECT visibility)
- Soft delete is the default; Hard delete requires SUPERVISOR access level

**Properties:**
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `noteId` | NoteId | Yes | Unique identifier |
| `title` | NoteTitle | Yes | Brief title (max 200 chars) |
| `content` | NoteContent | Yes | Body of the note |
| `category` | NoteCategory | Yes | **Mandatory** predefined classification |
| `visibility` | NoteVisibility | Yes | Who can see this note |
| `authorId` | AuthorId | Yes | The creator of the note |
| `projectId` | ProjectId | No | Project link (required for PROJECT visibility) |
| `tagIds` | Set\<TagId\> | No | Optional flexible keywords |
| `recipients` | Set\<Recipient\> | No | Users the note is shared with |
| `comments` | List\<Comment\> | No | Discussion thread |
| `status` | NoteStatus | Yes | ACTIVE, ARCHIVED, or DELETED |
| `auditInfo` | AuditInfo | Yes | Timestamps |

---

### Category (NoteCategory)

| Attribute | Description |
|-----------|-------------|
| **Definition** | A **mandatory**, **predefined** classification that identifies the nature of a note. Unlike Tags which are flexible keywords, a Category provides a structured organization of notes. |
| **French Equivalent** | Catégorie |
| **Business Rule** | Every note MUST have exactly ONE category. Categories are predefined by the system and cannot be created by users. |

**Predefined Categories:**

| Category | Description | Use Case |
|----------|-------------|----------|
| `BUG` | Bug report or known issue | "React state not updating in useEffect" |
| `TIP` | Useful tip or shortcut | "VSCode shortcut: Ctrl+Shift+P" |
| `DOCUMENTATION` | Documentation or explanation | "How our authentication flow works" |
| `QUESTION` | Question seeking answers | "How to optimize this SQL query?" |
| `ANNOUNCEMENT` | Team announcement | "React 19 is now available" |
| `RECOMMENDATION` | Personal recommendation | "Great article on clean architecture" |
| `BUSINESS_RULE` | Business rule documentation | "Discount calculation formula" |
| `CODE_SNIPPET` | Reusable code example | "Generic repository pattern in Java" |

**Difference with Tag:**
```
Category = WHAT TYPE of note is this? (mandatory, single, predefined)
Tag      = WHAT TOPICS does it cover? (optional, multiple, user-defined)

Example:
  Category: QUESTION
  Tags: [java, performance, database, hibernate]
```

---

### Tag (Label / Étiquette)

| Attribute | Description |
|-----------|-------------|
| **Definition** | A **flexible**, **user-defined** keyword attached to a Note for filtering and searchability. Unlike Categories, Tags are not predefined and users can create new ones. |
| **French Equivalent** | Étiquette / Tag |
| **Business Rule** | A Note CAN have zero or more Tags. Tags are shared across all notes (global vocabulary). |

**Properties:**
| Property | Type | Description |
|----------|------|-------------|
| `tagId` | TagId | Unique identifier |
| `name` | TagName | Unique name (lowercase, max 50 chars) |
| `usageCount` | int | Number of notes using this tag |
| `auditInfo` | AuditInfo | Timestamps |

**Examples:** `java`, `react`, `performance`, `security`, `api-v2`, `memory-app`

---

### Note Visibility

| Visibility | Description | Access Rule |
|------------|-------------|-------------|
| `PRIVATE` | Only Author can see | `user is Author` |
| `SHARED` | All team members can see | `trustScope == TEAM_MEMBER` |
| `PROJECT` | Only project members can see | `user is ProjectMember` |

---

### Note Status

| Status | Description | Who Can See |
|--------|-------------|-------------|
| `ACTIVE` | Visible and modifiable | Per visibility rules |
| `ARCHIVED` | Read-only | Author + SUPERVISOR only |
| `DELETED` | Soft-deleted | SUPERVISOR only |

---

## 💬 Discussion & Comments

### Discussion

| Attribute | Description |
|-----------|-------------|
| **Definition** | A structured exchange between team members around a Note, enabling idea sharing, problem resolution, and decision making. A Discussion is the collection of all Comments on a Note. |
| **French Equivalent** | Discussion / Fil de Discussion |
| **Business Rule** | Every Note has a Discussion (which may be empty). Comments within a Discussion can be threaded (replies). |

**Characteristics:**
- Organized as conversation threads
- Supports asynchronous collaboration
- Can lead to validated Solutions
- Triggers notifications for followers

---

### Comment

| Attribute | Description |
|-----------|-------------|
| **Definition** | A textual message added to a Note's Discussion to exchange information, ask questions, or propose solutions. |
| **French Equivalent** | Commentaire |

**Properties:**
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `commentId` | CommentId | Yes | Unique identifier |
| `authorId` | AuthorId | Yes | Who wrote the comment |
| `content` | String | Yes | Text content (max 5000 chars) |
| `parentCommentId` | CommentId | No | For threaded replies |
| `solutionStatus` | SolutionStatus | Yes | REGULAR, PROPOSED, or VALIDATED |
| `moderationStatus` | ModerationStatus | Yes | VISIBLE, CENSORED, or UNDER_REVIEW |
| `auditInfo` | AuditInfo | Yes | Timestamps |

**Invariants:**
- A Comment MUST belong to exactly one Note
- A Comment MUST have non-empty content
- CommentAuthor can edit/delete their own comments
- Only Note Author can validate a Solution

---

### Solution

| Attribute | Description |
|-----------|-------------|
| **Definition** | A Comment that answers or resolves the question/problem raised in the Note. Solutions can be proposed by any commenter and validated by the Note Author. |
| **French Equivalent** | Solution |
| **Business Rule** | A validated Solution is highlighted and can be used to update the Note content. |

**Solution Status (SolutionStatus):**

| Status | Description | Who Can Set |
|--------|-------------|-------------|
| `REGULAR` | Normal comment, not a solution | Default |
| `PROPOSED` | Commenter proposes this as a solution | CommentAuthor |
| `VALIDATED` | Note Author validated this solution | Note Author or SUPERVISOR |

**Solution Workflow:**
```
1. User creates a Note (e.g., Category: QUESTION)
2. Contributors add Comments to the Discussion
3. A contributor marks their Comment as PROPOSED solution
4. Note Author reviews and marks it as VALIDATED
5. The validated Solution is highlighted in the Discussion
6. (Optional) Note Author updates Note content to integrate the Solution
```

---

### ModerationStatus

| Status | Description |
|--------|-------------|
| `VISIBLE` | Comment is visible to all with note access |
| `CENSORED` | Hidden by moderator; shows "[Content removed by moderator]" |
| `UNDER_REVIEW` | Flagged and awaiting moderation |

---

## 👤 Contextual Roles (BC Note Specific)

### Author

| Attribute | Description |
|-----------|-------------|
| **Definition** | The user who created a Note. Has full control over their note. |
| **Determination** | `user.userId == note.authorId` |

**Capabilities:**
- ✅ View, edit, archive, delete their note
- ✅ Change visibility, share with Recipients
- ✅ Add/remove Tags, change Category
- ✅ Moderate all comments on their note
- ✅ **Validate Solutions** proposed in comments

### CommentAuthor

| Attribute | Description |
|-----------|-------------|
| **Definition** | The user who wrote a Comment. |
| **Determination** | `user.userId == comment.authorId` |

**Capabilities:**
- ✅ Edit/delete their own comment
- ✅ **Propose** their comment as a Solution

### Recipient

| Attribute | Description |
|-----------|-------------|
| **Definition** | A user with whom a Note has been explicitly shared. |
| **Determination** | `note.recipients.contains(user.userId)` |

**Capabilities:**
- ✅ View the shared note
- ✅ Comment (if accessLevel allows)

### ProjectMember

| Attribute | Description |
|-----------|-------------|
| **Definition** | A user who is a contributor of the linked project. |
| **Determination** | `projectAccessPort.isContributor(note.projectId, user.userId)` |

**Capabilities:**
- ✅ View PROJECT notes of their project
- ✅ Comment (if accessLevel allows)

---

## 📋 Access Policy Rules

### Viewing a Note

```
canViewNote(note, noteUserContext):
  IF user is Author → ALLOW
  IF user is Recipient → ALLOW
  IF noteUserContext.accessLevel == SUPERVISOR → ALLOW
  
  SWITCH note.visibility:
    PRIVATE → DENY
    SHARED  → ALLOW if trustScope == TEAM_MEMBER
    PROJECT → ALLOW if user is ProjectMember
```

### Commenting on a Note

```
canComment(note, noteUserContext):
  IF NOT canViewNote → DENY
  IF note.status != ACTIVE → DENY
  IF accessLevel == VIEWER → DENY
  → ALLOW
```

### Proposing a Solution

```
canProposeSolution(comment, noteUserContext):
  IF user is NOT comment.Author → DENY
  IF comment.solutionStatus != REGULAR → DENY
  → ALLOW
```

### Validating a Solution

```
canValidateSolution(note, comment, noteUserContext):
  IF user is note.Author → ALLOW
  IF accessLevel == SUPERVISOR → ALLOW
  → DENY
```

### Moderating Comments

```
canModerateComment(note, comment, noteUserContext):
  IF user is note.Author → ALLOW
  IF user is comment.Author → ALLOW (edit/delete only)
  IF accessLevel == SUPERVISOR → ALLOW
  IF accessLevel == MODERATOR AND user is ProjectOwner → ALLOW
  → DENY
```

---

## 📊 Value Objects

### NoteCategory (Enum)

```java
public enum NoteCategory {
    BUG,
    TIP,
    DOCUMENTATION,
    QUESTION,
    ANNOUNCEMENT,
    RECOMMENDATION,
    BUSINESS_RULE,
    CODE_SNIPPET
}
```

### SolutionStatus (Enum)

```java
public enum SolutionStatus {
    REGULAR,      // Normal comment
    PROPOSED,     // Proposed as solution by commenter
    VALIDATED     // Validated by note author
}
```

### NoteContent

| Property | Type | Description |
|----------|------|-------------|
| `body` | String | Text content (max 50,000 chars) |
| `contentType` | ContentType | TEXT, CODE, IMAGE, or MIXED |

### Recipient

| Property | Type | Description |
|----------|------|-------------|
| `userId` | AuthorId | The recipient's user ID |
| `sharedAt` | Instant | When the note was shared |

---

## 🔄 Domain Events

| Event | Description |
|-------|-------------|
| `NoteCreated` | New note created |
| `NoteUpdated` | Note content/title modified |
| `NoteCategoryChanged` | Category changed |
| `NoteVisibilityChanged` | Visibility changed |
| `NoteSharedWithRecipient` | Note shared with user |
| `NoteUnsharedWithRecipient` | Sharing removed |
| `NoteArchived` | Note archived |
| `NoteRestored` | Note restored from archive |
| `NoteDeleted` | Note soft-deleted |
| `TagAddedToNote` | Tag attached |
| `TagRemovedFromNote` | Tag removed |
| `CommentAdded` | New comment in discussion |
| `CommentUpdated` | Comment edited |
| `CommentCensored` | Comment moderated |
| `SolutionProposed` | Comment marked as proposed solution |
| `SolutionValidated` | Solution validated by note author |

---

## 🔌 Ports (Interfaces to External BCs)

### ProjectAccessPort (to BC Project)

```java
public interface ProjectAccessPort {
    boolean isContributor(UUID projectId, UUID userId);
    boolean isOwner(UUID projectId, UUID userId);
    boolean exists(UUID projectId);
}
```

---

## 🚨 Business Rules Summary

1. **Note ≠ Task**: Notes are informative; Tasks are actionable
2. **Category is Mandatory**: Every note MUST have exactly one predefined Category
3. **Tags are Optional**: Notes CAN have multiple user-defined Tags
4. **SHARED ≠ Public**: SHARED notes visible only to TEAM_MEMBER users
5. **Discussion-Centric**: Notes can have threaded comment discussions
6. **Solution Workflow**: Comments can be proposed and validated as Solutions
7. **Only Author Validates**: Only Note Author (or SUPERVISOR) can validate Solutions
8. **Soft Delete Default**: Hard delete requires SUPERVISOR access
9. **VIEWER Read-Only**: VIEWER access level cannot comment

---

## 📋 Aggregates

### Note Aggregate (Root)

```
Note (Aggregate Root)
├── NoteId (Identity)
├── AuthorId
├── NoteTitle (VO)
├── NoteContent (VO)
├── NoteCategory (Enum) ← NEW: mandatory
├── NoteVisibility (Enum)
├── NoteStatus (Enum)
├── ProjectId? (optional)
├── Set<TagId> (references)
├── Set<Recipient> (VOs)
├── List<Comment> (Entities) ← Discussion
│   ├── CommentId
│   ├── AuthorId
│   ├── Content
│   ├── ParentCommentId?
│   ├── SolutionStatus ← NEW
│   ├── ModerationStatus
│   └── AuditInfo
├── AuditInfo
└── ArchivedAt?
```

### Tag Aggregate (Root)

```
Tag (Aggregate Root)
├── TagId (Identity)
├── TagName (VO, unique)
├── UsageCount
└── AuditInfo
```

---

## 📝 Glossary Quick Reference

| Term | Type | Definition |
|------|------|------------|
| **Author** | Contextual Role | Creator of the note, full control |
| **CommentAuthor** | Contextual Role | Creator of a comment |
| **Recipient** | Contextual Role | User note is shared with |
| **ProjectMember** | Contextual Role | Contributor of linked project |
| **Category** | Classification | Mandatory, predefined (BUG, TIP, QUESTION...) |
| **Tag** | Classification | Optional, user-defined keywords |
| **Discussion** | Concept | Collection of comments on a note |
| **Solution** | Comment State | Comment validated as answer |
| **SUPERVISOR** | AccessLevel | View all, moderate all |
| **MODERATOR** | AccessLevel | Moderate in owned projects |
| **COMMENTER** | AccessLevel | Can comment |
| **VIEWER** | AccessLevel | Read-only |
| **TEAM_MEMBER** | TrustScope | Sees SHARED notes |
| **RESTRICTED_USER** | TrustScope | Only sees shared content |