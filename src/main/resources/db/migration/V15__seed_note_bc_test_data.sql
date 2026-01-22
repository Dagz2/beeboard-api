-- ============================================
-- Seed Test Notes for Front-End Development
-- Based on Community and The Office characters
-- ============================================

BEGIN;

-- ============================================
-- PART 1: CREATE TAGS (15 tags)
-- ============================================

INSERT INTO tag (name, usage_count, created_at, updated_at) VALUES
('java', 0, NOW(), NOW()),
('react', 0, NOW(), NOW()),
('bug-fix', 0, NOW(), NOW()),
('best-practices', 0, NOW(), NOW()),
('security', 0, NOW(), NOW()),
('performance', 0, NOW(), NOW()),
('database', 0, NOW(), NOW()),
('devops', 0, NOW(), NOW()),
('testing', 0, NOW(), NOW()),
('documentation', 0, NOW(), NOW()),
('code-review', 0, NOW(), NOW()),
('architecture', 0, NOW(), NOW()),
('api-design', 0, NOW(), NOW()),
('frontend', 0, NOW(), NOW()),
('meeting-notes', 0, NOW(), NOW());

-- ============================================
-- PART 2: CREATE NOTES - BATCH 1 (10 notes)
-- ============================================

-- Note 1: Kevin - Simple Bug Report
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Why number no work?',
    'I try add number but get weird result. When I do 5 + "3" I get "53" but should be 8. Help please?',
    'TEXT',
    'BUG',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '1 hour'
FROM users WHERE email = 'kevin.malone@memory-dev.com';

-- Note 2: Dwight - Security Bug Report
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'CRITICAL: Password stored in plain text',
    'Found passwords stored without encryption in production database. This is a security vulnerability of the highest order. Must fix immediately.',
    'TEXT',
    'BUG',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '5 hours',
    NOW() - INTERVAL '30 minutes'
FROM users WHERE email = 'dwight.schrute@memory-dev.com';

-- Note 3: Jim - React Tip
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Quick tip: useCallback for expensive operations',
    'When you have expensive calculations, wrap them in useCallback to avoid re-renders. Example: const expensiveCalc = useCallback(() => { return heavyOperation(); }, [dependency]);',
    'CODE',
    'TIP',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 hours'
FROM users WHERE email = 'jim.halpert@memory-dev.com';

-- Note 4: Pam - Documentation Request
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'API documentation is outdated',
    'The authentication endpoints documentation does not match the actual implementation. Can someone update the swagger file?',
    'TEXT',
    'DOCUMENTATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '3 hours'
FROM users WHERE email = 'pam.beesly@memory-dev.com';

-- Note 5: Angela - Code Review Best Practice
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Code review checklist',
    'Before approving PRs: 1) Check for test coverage 2) Verify no hardcoded values 3) Look for security issues 4) Ensure naming conventions 5) Check for code duplication',
    'TEXT',
    'RECOMMENDATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '4 days',
    NOW() - INTERVAL '1 day'
FROM users WHERE email = 'angela.martin@memory-dev.com';

-- Note 6: Oscar - Database Performance Question
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Should we add index on user_email?',
    'We have a query that searches users by email frequently. Would adding an index improve performance? What are the trade-offs?',
    'TEXT',
    'QUESTION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '6 hours',
    NOW() - INTERVAL '2 hours'
FROM users WHERE email = 'oscar.martinez@memory-dev.com';

-- Note 7: Michael - Announcement
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Pizza Party Friday!',
    'Team, great work this week! Friday at 5pm we will have pizza in the break room. Please RSVP so I know how many pizzas to order. That''s what she said.',
    'TEXT',
    'ANNOUNCEMENT',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '8 hours',
    NOW() - INTERVAL '7 hours'
FROM users WHERE email = 'michael.scott@memory-dev.com';

-- Note 8: Toby - HR Business Rule
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'PTO request approval workflow',
    'All PTO requests must be submitted at least 2 weeks in advance. Manager approval required within 3 business days. HR notified automatically.',
    'TEXT',
    'BUSINESS_RULE',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '9 days'
FROM users WHERE email = 'toby.flenderson@memory-dev.com';

-- Note 9: Stanley - Private Note
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Crossword puzzle strategies',
    'Best tips for completing crossword puzzles quickly: 1) Start with fill-in-the-blanks 2) Look for plural endings 3) Use pencil first',
    'TEXT',
    'TIP',
    'PRIVATE',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '4 days'
FROM users WHERE email = 'stanley.hudson@memory-dev.com';

-- Note 10: Phyllis - Code Snippet
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Java date formatting utility',
    'public static String formatDate(Instant instant) { DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault()); return formatter.format(instant); }',
    'CODE',
    'CODE_SNIPPET',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '6 days'
FROM users WHERE email = 'phyllis.vance@memory-dev.com';

-- ============================================
-- PART 3: CREATE NOTES - BATCH 2 (10 notes)
-- ============================================

-- Note 11: Creed - Strange Question
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'How delete production database?',
    'Need to know command for deleting all data from production. Is it DROP DATABASE or DELETE FROM *? Urgent.',
    'TEXT',
    'QUESTION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '30 minutes'
FROM users WHERE email = 'creed.bratton@memory-dev.com';

-- Note 12: Ryan - API Design Recommendation
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'RESTful API naming conventions',
    'Use nouns for resources, not verbs. Use plural forms. Use hyphens for multi-word resources. Example: GET /api/users-profiles not GET /api/getUserProfiles',
    'TEXT',
    'RECOMMENDATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '2 days'
FROM users WHERE email = 'ryan.howard@memory-dev.com';

-- Note 13: Kelly - Frontend Bug
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Button color is wrong on mobile',
    'The submit button shows as pink on iPhone but blue on Android. It should be our brand color (#007bff) on both. Please fix this is literally so annoying.',
    'TEXT',
    'BUG',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '4 hours',
    NOW() - INTERVAL '2 hours'
FROM users WHERE email = 'kelly.kapoor@memory-dev.com';

-- Note 14: Andy - Meeting Notes
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Sprint Planning - Week 48',
    'Attendees: Dev team + PO. Topics: 1) Review last sprint velocity 2) Prioritize backlog 3) Estimate new stories 4) Discuss technical debt. Action items: Update Jira by EOD.',
    'TEXT',
    'ANNOUNCEMENT',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '9 days',
    NOW() - INTERVAL '8 days'
FROM users WHERE email = 'andy.bernard@memory-dev.com';

-- Note 15: Erin - Documentation
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'How to run the project locally',
    'Step 1: Clone repo. Step 2: Install Docker. Step 3: Run docker-compose up. Step 4: Visit localhost:8080. Step 5: Login with test credentials from README!',
    'TEXT',
    'DOCUMENTATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '14 days'
FROM users WHERE email = 'erin.hannon@memory-dev.com';

-- Note 16: Darryl - DevOps Tip
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Docker container optimization',
    'Use multi-stage builds to reduce image size. Example: First stage compiles, second stage only copies artifacts. Reduced our image from 800MB to 150MB.',
    'TEXT',
    'TIP',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '6 days',
    NOW() - INTERVAL '5 days'
FROM users WHERE email = 'darryl.philbin@memory-dev.com';

-- Note 17: Jeff - Architecture Design
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Microservices vs Monolith for our use case',
    'Given our team size (10 devs) and application complexity, a modular monolith with bounded contexts makes more sense than full microservices. Easier deployment, less operational overhead.',
    'TEXT',
    'RECOMMENDATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '12 days',
    NOW() - INTERVAL '11 days'
FROM users WHERE email = 'jeff.winger@memory-dev.com';

-- Note 18: Britta - Security Best Practice
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Always validate user input',
    'Never trust client-side validation alone. Always sanitize and validate on the server. Use parameterized queries to prevent SQL injection. Escape HTML to prevent XSS.',
    'TEXT',
    'RECOMMENDATION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '7 days'
FROM users WHERE email = 'britta.perry@memory-dev.com';

-- Note 19: Abed - Code Snippet
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Retry logic with exponential backoff',
    'const retry = async (fn, maxRetries = 3) => { for (let i = 0; i < maxRetries; i++) { try { return await fn(); } catch (err) { if (i === maxRetries - 1) throw err; await sleep(2 ** i * 1000); } } };',
    'CODE',
    'CODE_SNIPPET',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '4 days'
FROM users WHERE email = 'abed.nadir@memory-dev.com';

-- Note 20: Troy - Testing Question
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'Unit tests vs Integration tests?',
    'What is the difference and when should I use each? I wrote tests but not sure if they are the right kind. Help!',
    'TEXT',
    'QUESTION',
    'SHARED',
    'ACTIVE',
    user_id,
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '1 day'
FROM users WHERE email = 'troy.barnes@memory-dev.com';

-- ============================================
-- PART 4: CREATE NOTES - BATCH 3 (10 notes)
-- ============================================

-- Note 21-30: Additional varied notes (simplified for brevity)
INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Git branching strategy', 'We should use Git Flow: main for production, develop for integration, feature/* for new features.', 'TEXT', 'RECOMMENDATION', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'
FROM users WHERE email = 'annie.edison@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'CSS Grid vs Flexbox', 'Use Grid for 2D layouts, Flexbox for 1D layouts. Grid is better for page structure, Flex for components.', 'TEXT', 'TIP', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'
FROM users WHERE email = 'shirley.bennett@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'API rate limiting implementation', 'Implemented rate limiting: 100 requests per minute per user. Using Redis to track counts. Returns 429 Too Many Requests when exceeded.', 'TEXT', 'DOCUMENTATION', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'
FROM users WHERE email = 'pierce.hawthorne@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Null pointer exception in UserService', 'Getting NPE when user has no email set. Need to add null check before calling toLowerCase().', 'TEXT', 'BUG', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '1 day', NOW() - INTERVAL '5 hours'
FROM users WHERE email = 'ben.chang@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Database backup policy', 'Automated daily backups at 2 AM. Retention: 7 daily, 4 weekly, 12 monthly. Stored in S3 with encryption.', 'TEXT', 'BUSINESS_RULE', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days'
FROM users WHERE email = 'michael.scott@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'How to debug production issues?', 'What tools do we have for debugging production? Can I access logs? Do we have APM monitoring?', 'TEXT', 'QUESTION', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '1 hour'
FROM users WHERE email = 'kevin.malone@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Password reset flow diagram', 'User clicks Forgot Password -> Enter email -> Receive token -> Click link -> Enter new password -> Success. Token expires in 1 hour.', 'TEXT', 'DOCUMENTATION', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'
FROM users WHERE email = 'pam.beesly@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Java Stream API cheatsheet', 'filter() -> keep elements matching condition. map() -> transform elements. reduce() -> combine to single value. collect() -> gather to collection.', 'TEXT', 'TIP', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'
FROM users WHERE email = 'oscar.martinez@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'React component lifecycle', 'componentDidMount -> Component added to DOM. componentDidUpdate -> Props or state changed. componentWillUnmount -> Component removed from DOM.', 'TEXT', 'DOCUMENTATION', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days'
FROM users WHERE email = 'jim.halpert@memory-dev.com';

INSERT INTO note (note_id, title, content, content_type, category, visibility, status, author_id, created_at, updated_at)
SELECT gen_random_uuid(), 'Performance monitoring setup', 'Added Prometheus for metrics collection and Grafana for visualization. Dashboards show CPU, memory, request rates, and response times.', 'TEXT', 'ANNOUNCEMENT', 'SHARED', 'ACTIVE', user_id, NOW() - INTERVAL '11 days', NOW() - INTERVAL '10 days'
FROM users WHERE email = 'dwight.schrute@memory-dev.com';

-- ============================================
-- PART 5: LINK TAGS TO NOTES (note_tag)
-- ============================================

-- Kevin's bug note gets: java, bug-fix
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Why number no work?' AND t.name IN ('java', 'bug-fix');

-- Dwight's security bug: security, bug-fix
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'CRITICAL: Password stored in plain text' AND t.name IN ('security', 'bug-fix');

-- Jim's React tip: react, frontend, best-practices
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Quick tip: useCallback for expensive operations' AND t.name IN ('react', 'frontend', 'best-practices');

-- Pam's documentation: documentation
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'API documentation is outdated' AND t.name = 'documentation';

-- Angela's code review: code-review, best-practices
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Code review checklist' AND t.name IN ('code-review', 'best-practices');

-- Oscar's database question: database, performance
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Should we add index on user_email?' AND t.name IN ('database', 'performance');

-- Toby's business rule: documentation
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'PTO request approval workflow' AND t.name = 'documentation';

-- Phyllis's code snippet: java
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Java date formatting utility' AND t.name = 'java';

-- Ryan's API design: api-design, architecture
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'RESTful API naming conventions' AND t.name IN ('api-design', 'architecture');

-- Kelly's frontend bug: frontend, bug-fix
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Button color is wrong on mobile' AND t.name IN ('frontend', 'bug-fix');

-- Andy's meeting notes: meeting-notes
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Sprint Planning - Week 48' AND t.name = 'meeting-notes';

-- Erin's documentation: documentation
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'How to run the project locally' AND t.name = 'documentation';

-- Darryl's devops tip: devops
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Docker container optimization' AND t.name = 'devops';

-- Jeff's architecture: architecture
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Microservices vs Monolith for our use case' AND t.name = 'architecture';

-- Britta's security: security, best-practices
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Always validate user input' AND t.name IN ('security', 'best-practices');

-- Abed's code snippet: java
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Retry logic with exponential backoff' AND t.name = 'java';

-- Troy's testing question: testing
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Unit tests vs Integration tests?' AND t.name = 'testing';

-- Annie's git note: devops
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Git branching strategy' AND t.name = 'devops';

-- Shirley's CSS note: frontend
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'CSS Grid vs Flexbox' AND t.name = 'frontend';

-- More tag assignments for remaining notes
INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'API rate limiting implementation' AND t.name IN ('api-design', 'security');

INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Null pointer exception in UserService' AND t.name IN ('java', 'bug-fix');

INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Database backup policy' AND t.name IN ('database', 'devops');

INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Java Stream API cheatsheet' AND t.name = 'java';

INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'React component lifecycle' AND t.name IN ('react', 'frontend');

INSERT INTO note_tag (note_id, tag_id, created_at)
SELECT n.note_id, t.tag_id, NOW()
FROM note n, tag t
WHERE n.title = 'Performance monitoring setup' AND t.name IN ('devops', 'performance');

-- ============================================
-- PART 6: ADD COMMENTS (with threading)
-- ============================================

-- Comment on Kevin's bug note from Ryan (solution)
INSERT INTO comment (comment_id, note_id, author_id, content, solution_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Kevin, the issue is that you are concatenating a number with a string. In JavaScript, 5 + "3" results in string concatenation, not addition. Use parseInt() to convert the string to a number first: 5 + parseInt("3") will give you 8.',
    'PROPOSED',
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours'
FROM note n, users u
WHERE n.title = 'Why number no work?' AND u.email = 'ryan.howard@memory-dev.com';

-- Kevin replies to Ryan (thread)
INSERT INTO comment (comment_id, note_id, author_id, content, parent_comment_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Oh that make sense! Thank you Ryan you are smart.',
    c.comment_id,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour'
FROM note n, users u, comment c, users u2
WHERE n.title = 'Why number no work?'
  AND u.email = 'kevin.malone@memory-dev.com'
  AND c.note_id = n.note_id
  AND u2.email = 'ryan.howard@memory-dev.com'
  AND c.author_id = u2.user_id;

-- Jim validates Ryan's solution
INSERT INTO comment (comment_id, note_id, author_id, content, solution_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Ryan is correct. Marking this as validated solution.',
    'VALIDATED',
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes'
FROM note n, users u
WHERE n.title = 'Why number no work?' AND u.email = 'jim.halpert@memory-dev.com';

-- Comment on Dwight's security bug from Toby
INSERT INTO comment (comment_id, note_id, author_id, content, moderation_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'This is a critical issue. I will escalate to the security team immediately. All developers must attend the security training next week.',
    'VISIBLE',
    NOW() - INTERVAL '20 minutes',
    NOW() - INTERVAL '20 minutes'
FROM note n, users u
WHERE n.title = 'CRITICAL: Password stored in plain text' AND u.email = 'toby.flenderson@memory-dev.com';

-- Dwight replies to Toby
INSERT INTO comment (comment_id, note_id, author_id, content, parent_comment_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Excellent. Security is no joke. I volunteer to lead the training. I have experience in surveillance and counter-intelligence.',
    c.comment_id,
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '10 minutes'
FROM note n, users u, comment c, users u2
WHERE n.title = 'CRITICAL: Password stored in plain text'
  AND u.email = 'dwight.schrute@memory-dev.com'
  AND c.note_id = n.note_id
  AND u2.email = 'toby.flenderson@memory-dev.com'
  AND c.author_id = u2.user_id;

-- Comment on Oscar's database question from Angela
INSERT INTO comment (comment_id, note_id, author_id, content, solution_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Yes, adding an index on email would significantly improve query performance. The trade-off is slightly slower INSERT/UPDATE operations and additional storage space. Since we search by email frequently, the index is worth it.',
    'PROPOSED',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour'
FROM note n, users u
WHERE n.title = 'Should we add index on user_email?' AND u.email = 'angela.martin@memory-dev.com';

-- Comment on Creed's dangerous question from Jim
INSERT INTO comment (comment_id, note_id, author_id, content, moderation_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Creed, please DO NOT run any delete commands on production! This note is being flagged for review.',
    'UNDER_REVIEW',
    NOW() - INTERVAL '15 minutes',
    NOW() - INTERVAL '15 minutes'
FROM note n, users u
WHERE n.title = 'How delete production database?' AND u.email = 'jim.halpert@memory-dev.com';

-- Comment on Troy's testing question from Britta (educational)
INSERT INTO comment (comment_id, note_id, author_id, content, solution_status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Unit tests test individual functions in isolation. Integration tests test how multiple components work together. Unit tests are fast, integration tests are slower but catch interaction bugs. You need both!',
    'PROPOSED',
    NOW() - INTERVAL '12 hours',
    NOW() - INTERVAL '12 hours'
FROM note n, users u
WHERE n.title = 'Unit tests vs Integration tests?' AND u.email = 'britta.perry@memory-dev.com';

-- Troy thanks Britta
INSERT INTO comment (comment_id, note_id, author_id, content, parent_comment_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    n.note_id,
    u.user_id,
    'Thanks Britta! That makes so much sense now. Cool cool cool.',
    c.comment_id,
    NOW() - INTERVAL '11 hours',
    NOW() - INTERVAL '11 hours'
FROM note n, users u, comment c, users u2
WHERE n.title = 'Unit tests vs Integration tests?'
  AND u.email = 'troy.barnes@memory-dev.com'
  AND c.note_id = n.note_id
  AND u2.email = 'britta.perry@memory-dev.com'
  AND c.author_id = u2.user_id;

-- ============================================
-- PART 7: ADD NOTE RECIPIENTS (sharing)
-- ============================================

-- Michael shares his pizza announcement with specific people
INSERT INTO note_recipient (note_id, user_id, shared_at)
SELECT n.note_id, u.user_id, NOW() - INTERVAL '7 hours'
FROM note n, users u
WHERE n.title = 'Pizza Party Friday!' AND u.email IN ('jim.halpert@memory-dev.com', 'pam.beesly@memory-dev.com', 'dwight.schrute@memory-dev.com');

-- Toby shares HR business rule with managers
INSERT INTO note_recipient (note_id, user_id, shared_at)
SELECT n.note_id, u.user_id, NOW() - INTERVAL '9 days'
FROM note n, users u
WHERE n.title = 'PTO request approval workflow' AND u.email IN ('michael.scott@memory-dev.com', 'jeff.winger@memory-dev.com');

-- Angela shares code review checklist with team leads
INSERT INTO note_recipient (note_id, user_id, shared_at)
SELECT n.note_id, u.user_id, NOW() - INTERVAL '1 day'
FROM note n, users u
WHERE n.title = 'Code review checklist' AND u.email IN ('jim.halpert@memory-dev.com', 'ryan.howard@memory-dev.com', 'britta.perry@memory-dev.com');

-- Jeff shares architecture decision with senior devs
INSERT INTO note_recipient (note_id, user_id, shared_at)
SELECT n.note_id, u.user_id, NOW() - INTERVAL '11 days'
FROM note n, users u
WHERE n.title = 'Microservices vs Monolith for our use case' AND u.email IN ('dwight.schrute@memory-dev.com', 'angela.martin@memory-dev.com', 'abed.nadir@memory-dev.com');

COMMIT;
