package org.integration;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.application.dto.command.user.ChangeFullNameCommand;
import org.access.application.usecase.command.user.ChangeFullName;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.access.support.TestFixtures;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;
import org.note.domain.model.AccessLevel;
import org.note.domain.model.AuthorView;
import org.note.domain.repository.AuthorViewRepository;
import org.shared.domain.AccountStatus;
import org.shared.domain.ApplicationRole;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Integration test verifying UserFullNameChangedEvent synchronization from Access BC to Note BC.
 *
 * <p>This test verifies the complete event flow:</p>
 * <ol>
 *   <li>Access BC: User name is changed via ChangeFullName use case</li>
 *   <li>Access BC: UserFullNameChangedEvent is published</li>
 *   <li>Note BC: Event handler receives event asynchronously</li>
 *   <li>Note BC: author_view.full_name is updated</li>
 *   <li>Note BC: subsequent queries return the new name</li>
 * </ol>
 */
@QuarkusTest
@DisplayName("UserFullNameChanged - Cross-BC Event Synchronization")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserFullNameChangeEventIT {

    private static final Logger LOG = Logger.getLogger(UserFullNameChangeEventIT.class);

    @Inject
    ChangeFullName changeFullName;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    AuthorViewRepository authorViewRepository;

    private static UserId testUserId;
    private static RoleId testRoleId;
    private static SecurityContext superAdminContext;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            LOG.info("Setting up UserFullNameChange integration test data");

            // Create a test role
            Role testRole = TestFixtures.aRole()
                .withCode("TEST_ROLE")
                .withName("Test Role")
                .withApplicationRole(ApplicationRole.MEMBER)
                .withTier(SecurityTier.TIER_2)
                .build();
            roleRepository.save(testRole);
            testRoleId = testRole.id();

            // Create a test user with initial name "John Doe"
            User testUser = TestFixtures.aUser()
                .withEmail("john.doe.namechange@example.com")
                .withFullName(new FullName("John", "Doe"))
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(testUser);
            testUserId = testUser.getId();

            // Create corresponding author_view entry
            AuthorView authorView = new AuthorView(
                testUserId.id(),
                AccessLevel.COMMENTER,
                AccountStatus.ACTIVE,
                "John Doe"
            );
            authorViewRepository.save(authorView);

            // Create super admin security context
            superAdminContext = TestFixtures.aSecurityContext()
                .asSuperAdmin()
                .build();

            setupComplete = true;
            LOG.info("UserFullNameChange test data setup complete");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should update author_view when user name changes in Access BC")
    @Transactional
    void shouldUpdateAuthorViewWhenUserNameChanges() {
        // Given - User exists with name "John Doe"
        UUID userId = testUserId.id();

        // Verify initial state
        Optional<AuthorView> initialAuthor = authorViewRepository.findById(userId);
        assertThat(initialAuthor).isPresent();
        assertThat(initialAuthor.get().fullName()).isEqualTo("John Doe");
        LOG.info("Verified initial state: author_view.full_name = 'John Doe'");

        // When - Change name to "Jonathan Smith" in Access BC
        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Jonathan",
            "Smith"
        );

        LOG.infof("Executing ChangeFullName command: %s -> Jonathan Smith", userId);
        changeFullName.execute(command, superAdminContext);
        LOG.info("ChangeFullName command executed, UserFullNameChangedEvent should be published");

        // Then - Wait for async event processing and verify author_view is updated
        await()
            .atMost(5, SECONDS)
            .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                Optional<AuthorView> updatedAuthor = authorViewRepository.findById(userId);
                assertThat(updatedAuthor).isPresent();
                assertThat(updatedAuthor.get().fullName()).isEqualTo("Jonathan Smith");
                LOG.info("Verified: author_view.full_name updated to 'Jonathan Smith'");
            });

        // Also verify the user was updated in Access BC
        User user = userRepository.findById(testUserId).orElseThrow();
        assertThat(user.getFullName().firstName()).isEqualTo("Jonathan");
        assertThat(user.getFullName().lastName()).isEqualTo("Smith");
        LOG.info("Test passed: Full name change synchronized from Access BC to Note BC");
    }

    @Test
    @Order(2)
    @DisplayName("Should update only full_name field and leave other fields unchanged")
    @Transactional
    void shouldUpdateOnlyFullNameFieldInAuthorView() {
        // Given - User with known author_view state
        UUID userId = testUserId.id();

        // Capture initial state (all fields)
        AuthorView initialState = authorViewRepository.findById(userId).orElseThrow();
        AccessLevel initialAccessLevel = initialState.accessLevel();
        AccountStatus initialAccountStatus = initialState.accountStatus();
        LOG.infof("Initial state: accessLevel=%s, accountStatus=%s, fullName=%s",
            initialAccessLevel, initialAccountStatus, initialState.fullName());

        // When - Change only the name
        ChangeFullNameCommand command = new ChangeFullNameCommand(
            userId,
            "Updated",
            "Name"
        );

        changeFullName.execute(command, superAdminContext);
        LOG.info("Name change executed");

        // Then - Verify only full_name changed, other fields remain the same
        await()
            .atMost(5, SECONDS)
            .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                AuthorView updatedState = authorViewRepository.findById(userId).orElseThrow();

                // full_name should be updated
                assertThat(updatedState.fullName()).isEqualTo("Updated Name");

                // Other fields should remain unchanged
                assertThat(updatedState.accessLevel()).isEqualTo(initialAccessLevel);
                assertThat(updatedState.accountStatus()).isEqualTo(initialAccountStatus);
                assertThat(updatedState.userId()).isEqualTo(userId);

                LOG.info("Verified: Only full_name field was updated, other fields unchanged");
            });

        LOG.info("Test passed: Selective field update works correctly");
    }

    @Test
    @Order(3)
    @DisplayName("Should handle multiple name changes for the same user")
    @Transactional
    void shouldHandleMultipleNameChangesForSameUser() {
        // Given - User with current name
        UUID userId = testUserId.id();

        // When - Change name multiple times
        LOG.info("Executing first name change: First Change");
        ChangeFullNameCommand command1 = new ChangeFullNameCommand(userId, "First", "Change");
        changeFullName.execute(command1, superAdminContext);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            AuthorView author = authorViewRepository.findById(userId).orElseThrow();
            assertThat(author.fullName()).isEqualTo("First Change");
        });
        LOG.info("First change verified");

        LOG.info("Executing second name change: Second Change");
        ChangeFullNameCommand command2 = new ChangeFullNameCommand(userId, "Second", "Change");
        changeFullName.execute(command2, superAdminContext);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            AuthorView author = authorViewRepository.findById(userId).orElseThrow();
            assertThat(author.fullName()).isEqualTo("Second Change");
        });
        LOG.info("Second change verified");

        // Then - Final state should reflect the last change
        AuthorView finalState = authorViewRepository.findById(userId).orElseThrow();
        assertThat(finalState.fullName()).isEqualTo("Second Change");

        LOG.info("Test passed: Multiple sequential name changes handled correctly");
    }
}
