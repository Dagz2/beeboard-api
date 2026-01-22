package org.access.support;

import org.access.domain.model.*;
import org.shared.domain.ApplicationRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test fixtures providing builders and factory methods for creating domain objects in tests.
 *
 * Usage:
 * <pre>
 *     User user = TestFixtures.aUser().build();
 *     Role role = TestFixtures.aRole().withTier(SecurityTier.TIER_1).build();
 * </pre>
 */
public final class TestFixtures {

    // ==================== Constants ====================

    public static final String VALID_EMAIL = "test@example.com";
    public static final String VALID_PASSWORD = "SecurePassword123!@#$%^&*()_+abcd";
    public static final String VALID_FIRST_NAME = "John";
    public static final String VALID_LAST_NAME = "Doe";

    private TestFixtures() {
        // Utility class
    }

    // ==================== Value Object Factories ====================

    public static EmailAddress validEmail() {
        return new EmailAddress(VALID_EMAIL);
    }

    public static EmailAddress emailOf(String email) {
        return new EmailAddress(email);
    }

    public static Password validPassword() {
        return Password.fromPlainText(VALID_PASSWORD);
    }

    public static Password passwordFromPlain(String plainPassword) {
        return Password.fromPlainText(plainPassword);
    }

    public static FullName validFullName() {
        return new FullName(VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    public static FullName fullNameOf(String firstName, String lastName) {
        return new FullName(firstName, lastName);
    }

    public static ContractEndDate futureContractEndDate() {
        return ContractEndDate.newContractEndDate(LocalDate.now().plusYears(1));
    }

    public static ContractEndDate contractEndDateOf(LocalDate date) {
        return new ContractEndDate(date);
    }

    public static UserId randomUserId() {
        return UserId.newId();
    }

    public static UserId userIdOf(UUID id) {
        return new UserId(id);
    }

    public static RoleId randomRoleId() {
        return RoleId.newId();
    }

    public static RoleId roleIdOf(UUID id) {
        return new RoleId(id);
    }

    public static PermissionId randomPermissionId() {
        return PermissionId.newId();
    }

    public static PermissionId permissionIdOf(UUID id) {
        return new PermissionId(id);
    }

    public static RoleCode roleCodeOf(String code) {
        return new RoleCode(code);
    }

    public static RoleName roleNameOf(String name) {
        return new RoleName(name);
    }

    public static PermissionCode permissionCodeOf(String code) {
        return new PermissionCode(code);
    }

    public static PermissionName permissionNameOf(String name) {
        return new PermissionName(name);
    }

    public static ResetToken validResetToken() {
        return ResetToken.generate();
    }

    public static AuditInfo newAuditInfo() {
        return AuditInfo.of(Instant.now());
    }

    // ==================== Builders ====================

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static RoleBuilder aRole() {
        return new RoleBuilder();
    }

    public static PermissionBuilder aPermission() {
        return new PermissionBuilder();
    }

    public static SecurityContextBuilder aSecurityContext() {
        return new SecurityContextBuilder();
    }

    public static PasswordResetRequestBuilder aPasswordResetRequest() {
        return new PasswordResetRequestBuilder();
    }

    // ==================== User Builder ====================

    public static class UserBuilder {
        private UserId id = UserId.newId();
        private FullName fullName = validFullName();
        private EmailAddress email = validEmail();
        private Password password = validPassword();
        private RoleId roleId = RoleId.newId();
        private ContractEndDate contractEndDate = futureContractEndDate();
        private boolean isLocked = false;
        private AuditInfo auditInfo = newAuditInfo();
        private Instant lastActivityDate = Instant.now();

        public UserBuilder withId(UserId id) {
            this.id = id;
            return this;
        }

        public UserBuilder withFullName(FullName fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserBuilder withFullName(String firstName, String lastName) {
            this.fullName = new FullName(firstName, lastName);
            return this;
        }

        public UserBuilder withEmail(EmailAddress email) {
            this.email = email;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = new EmailAddress(email);
            return this;
        }

        public UserBuilder withPassword(Password password) {
            this.password = password;
            return this;
        }

        public UserBuilder withRoleId(RoleId roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserBuilder withContractEndDate(ContractEndDate contractEndDate) {
            this.contractEndDate = contractEndDate;
            return this;
        }

        public UserBuilder withContractEndDate(LocalDate date) {
            this.contractEndDate = new ContractEndDate(date);
            return this;
        }

        public UserBuilder withExpiredContract() {
            this.contractEndDate = new ContractEndDate(LocalDate.now().minusDays(1));
            return this;
        }

        public UserBuilder locked() {
            this.isLocked = true;
            return this;
        }

        public UserBuilder unlocked() {
            this.isLocked = false;
            return this;
        }

        public User build() {
            return new User(id, fullName, email, password, roleId, contractEndDate, isLocked, auditInfo, lastActivityDate);
        }
    }

    // ==================== Role Builder ====================

    public static class RoleBuilder {
        private RoleId id = RoleId.newId();
        private RoleCode code = new RoleCode("TEST_ROLE");
        private RoleName name = new RoleName("Test Role");
        private Set<PermissionId> permissions = new HashSet<>();
        private AuditInfo auditInfo = newAuditInfo();
        private SecurityTier tier = SecurityTier.TIER_2;
        private boolean isSystem = false;
        private ApplicationRole applicationRole = ApplicationRole.MEMBER;

        public RoleBuilder withId(RoleId id) {
            this.id = id;
            return this;
        }

        public RoleBuilder withCode(String code) {
            this.code = new RoleCode(code);
            return this;
        }

        public RoleBuilder withName(String name) {
            this.name = new RoleName(name);
            return this;
        }

        public RoleBuilder withPermission(PermissionId permissionId) {
            this.permissions.add(permissionId);
            return this;
        }

        public RoleBuilder withPermissions(Set<PermissionId> permissions) {
            this.permissions = new HashSet<>(permissions);
            return this;
        }

        public RoleBuilder withTier(SecurityTier tier) {
            this.tier = tier;
            return this;
        }

        public RoleBuilder withApplicationRole(ApplicationRole applicationRole) {
            this.applicationRole = applicationRole;
            return this;
        }

        public RoleBuilder asSystem() {
            this.isSystem = true;
            return this;
        }

        public RoleBuilder asNonSystem() {
            this.isSystem = false;
            return this;
        }

        public RoleBuilder asSuperAdmin() {
            this.code = new RoleCode("SUPER_ADMIN");
            this.name = new RoleName("Super Administrator");
            this.tier = SecurityTier.TIER_0;
            this.isSystem = true;
            this.applicationRole = ApplicationRole.ADMIN;
            return this;
        }

        public RoleBuilder asDirector() {
            this.code = new RoleCode("DIRECTOR");
            this.name = new RoleName("Director");
            this.tier = SecurityTier.TIER_1;
            this.isSystem = true;
            this.applicationRole = ApplicationRole.SUPERVISOR;
            return this;
        }

        public RoleBuilder asHrManager() {
            this.code = new RoleCode("HR_MANAGER");
            this.name = new RoleName("HR Manager");
            this.tier = SecurityTier.TIER_2;
            this.isSystem = true;
            this.applicationRole = ApplicationRole.MANAGER;
            return this;
        }

        public Role build() {
            return new Role(id, code, name, permissions, auditInfo, tier, isSystem, applicationRole);
        }
    }

    // ==================== Permission Builder ====================

    public static class PermissionBuilder {
        private PermissionId id = PermissionId.newId();
        private PermissionCode code = new PermissionCode("TEST_PERMISSION");
        private PermissionName name = new PermissionName("Test Permission");
        private String description = "A test permission";
        private SecurityTier tier = SecurityTier.TIER_2;
        private boolean isSystem = false;

        public PermissionBuilder withId(PermissionId id) {
            this.id = id;
            return this;
        }

        public PermissionBuilder withCode(String code) {
            this.code = new PermissionCode(code);
            return this;
        }

        public PermissionBuilder withName(String name) {
            this.name = new PermissionName(name);
            return this;
        }

        public PermissionBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public PermissionBuilder withTier(SecurityTier tier) {
            this.tier = tier;
            return this;
        }

        public PermissionBuilder asSystem() {
            this.isSystem = true;
            return this;
        }

        public PermissionBuilder asNonSystem() {
            this.isSystem = false;
            return this;
        }

        public Permission build() {
            return new Permission(id, code, name, description, tier, isSystem);
        }
    }

    // ==================== SecurityContext Builder ====================

    public static class SecurityContextBuilder {
        private UserId actorId = UserId.newId();
        private String actorEmail = VALID_EMAIL;
        private RoleId roleId = RoleId.newId();
        private RoleCode roleCode = new RoleCode("TEST_ROLE");
        private SecurityTier actorTier = SecurityTier.TIER_2;
        private Set<PermissionCode> permissions = new HashSet<>();

        public SecurityContextBuilder withActorId(UserId actorId) {
            this.actorId = actorId;
            return this;
        }

        public SecurityContextBuilder withActorEmail(String email) {
            this.actorEmail = email;
            return this;
        }

        public SecurityContextBuilder withRoleId(RoleId roleId) {
            this.roleId = roleId;
            return this;
        }

        public SecurityContextBuilder withRoleCode(String roleCode) {
            this.roleCode = new RoleCode(roleCode);
            return this;
        }

        public SecurityContextBuilder withTier(SecurityTier tier) {
            this.actorTier = tier;
            return this;
        }

        public SecurityContextBuilder withPermission(String permissionCode) {
            this.permissions.add(new PermissionCode(permissionCode));
            return this;
        }

        public SecurityContextBuilder withPermissions(Set<PermissionCode> permissions) {
            this.permissions = new HashSet<>(permissions);
            return this;
        }

        public SecurityContextBuilder asSuperAdmin() {
            this.roleCode = new RoleCode("SUPER_ADMIN");
            this.actorTier = SecurityTier.TIER_0;
            return this;
        }

        public SecurityContextBuilder asDirector() {
            this.roleCode = new RoleCode("DIRECTOR");
            this.actorTier = SecurityTier.TIER_1;
            return this;
        }

        public SecurityContextBuilder asHrManager() {
            this.roleCode = new RoleCode("HR_MANAGER");
            this.actorTier = SecurityTier.TIER_2;
            return this;
        }

        public SecurityContext build() {
            return new SecurityContext(actorId, actorEmail, roleId, roleCode, actorTier, permissions);
        }
    }

    // ==================== PasswordResetRequest Builder ====================

    public static class PasswordResetRequestBuilder {
        private PasswordResetRequestId id = PasswordResetRequestId.newId();
        private UserId userId = UserId.newId();
        private ResetToken token = ResetToken.generate();
        private Instant expiresAt = Instant.now().plusSeconds(15 * 60);
        private boolean isUsed = false;
        private AuditInfo auditInfo = newAuditInfo();

        public PasswordResetRequestBuilder withId(PasswordResetRequestId id) {
            this.id = id;
            return this;
        }

        public PasswordResetRequestBuilder withUserId(UserId userId) {
            this.userId = userId;
            return this;
        }

        public PasswordResetRequestBuilder withToken(ResetToken token) {
            this.token = token;
            return this;
        }

        public PasswordResetRequestBuilder withExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public PasswordResetRequestBuilder expired() {
            this.expiresAt = Instant.now().minusSeconds(60);
            return this;
        }

        public PasswordResetRequestBuilder used() {
            this.isUsed = true;
            return this;
        }

        public PasswordResetRequest build() {
            return new PasswordResetRequest(id, userId, token, expiresAt, isUsed, auditInfo);
        }
    }
}
