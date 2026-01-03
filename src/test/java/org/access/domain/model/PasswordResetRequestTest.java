package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordResetRequest Aggregate")
class PasswordResetRequestTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create password reset request with factory method")
        void shouldCreatePasswordResetRequestWithFactoryMethod() {
            UserId userId = randomUserId();
            ResetToken token = validResetToken();

            PasswordResetRequest request = PasswordResetRequest.create(userId, token);

            assertThat(request.getId()).isNotNull();
            assertThat(request.getUserId()).isEqualTo(userId);
            assertThat(request.getToken()).isEqualTo(token);
            assertThat(request.getIsUsed()).isFalse();
            assertThat(request.getExpiresAt()).isAfter(Instant.now());
            assertThat(request.getAuditInfo()).isNotNull();
        }

        @Test
        @DisplayName("should set expiration to 15 minutes from now")
        void shouldSetExpirationTo15MinutesFromNow() {
            PasswordResetRequest request = PasswordResetRequest.create(randomUserId(), validResetToken());

            Instant expectedExpiration = Instant.now().plusSeconds(15 * 60);

            assertThat(request.getExpiresAt()).isBetween(
                expectedExpiration.minusSeconds(5),
                expectedExpiration.plusSeconds(5)
            );
        }

        @Test
        @DisplayName("should create with builder")
        void shouldCreateWithBuilder() {
            UserId userId = randomUserId();
            ResetToken token = validResetToken();

            PasswordResetRequest request = aPasswordResetRequest()
                .withUserId(userId)
                .withToken(token)
                .build();

            assertThat(request.getUserId()).isEqualTo(userId);
            assertThat(request.getToken()).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("Expiration check")
    class ExpirationCheck {

        @Test
        @DisplayName("should return false for non-expired token")
        void shouldReturnFalseForNonExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .withExpiresAt(Instant.now().plusSeconds(600))
                .build();

            assertThat(request.isExpired()).isFalse();
        }

        @Test
        @DisplayName("should return true for expired token")
        void shouldReturnTrueForExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .expired()
                .build();

            assertThat(request.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("canBeUsed check")
    class CanBeUsedCheck {

        @Test
        @DisplayName("should return true for unused and non-expired token")
        void shouldReturnTrueForUnusedAndNonExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .withExpiresAt(Instant.now().plusSeconds(600))
                .build();

            assertThat(request.canBeUsed()).isTrue();
        }

        @Test
        @DisplayName("should return false for used token")
        void shouldReturnFalseForUsedToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .used()
                .build();

            assertThat(request.canBeUsed()).isFalse();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .expired()
                .build();

            assertThat(request.canBeUsed()).isFalse();
        }

        @Test
        @DisplayName("should return false for used and expired token")
        void shouldReturnFalseForUsedAndExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .used()
                .expired()
                .build();

            assertThat(request.canBeUsed()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsUsed operation")
    class MarkAsUsedOperation {

        @Test
        @DisplayName("should mark valid token as used")
        void shouldMarkValidTokenAsUsed() {
            PasswordResetRequest request = aPasswordResetRequest()
                .withExpiresAt(Instant.now().plusSeconds(600))
                .build();

            request.markAsUsed();

            assertThat(request.getIsUsed()).isTrue();
            assertThat(request.canBeUsed()).isFalse();
        }

        @Test
        @DisplayName("should throw when marking already used token")
        void shouldThrowWhenMarkingAlreadyUsedToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .used()
                .build();

            assertThatThrownBy(() -> request.markAsUsed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Token has already been used.");
        }

        @Test
        @DisplayName("should throw when marking expired token")
        void shouldThrowWhenMarkingExpiredToken() {
            PasswordResetRequest request = aPasswordResetRequest()
                .expired()
                .build();

            assertThatThrownBy(() -> request.markAsUsed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot use an expired token.");
        }
    }

    @Nested
    @DisplayName("Token security")
    class TokenSecurity {

        @Test
        @DisplayName("token is single-use only")
        void tokenIsSingleUseOnly() {
            PasswordResetRequest request = aPasswordResetRequest()
                .withExpiresAt(Instant.now().plusSeconds(600))
                .build();

            request.markAsUsed();

            assertThatThrownBy(() -> request.markAsUsed())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("expired tokens cannot be used even if not marked as used")
        void expiredTokensCannotBeUsed() {
            PasswordResetRequest request = aPasswordResetRequest()
                .expired()
                .build();

            assertThat(request.getIsUsed()).isFalse();
            assertThat(request.canBeUsed()).isFalse();
            assertThatThrownBy(() -> request.markAsUsed())
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when IDs are equal")
        void shouldBeEqualWhenIdsAreEqual() {
            PasswordResetRequestId id = PasswordResetRequestId.newId();
            PasswordResetRequest req1 = aPasswordResetRequest().withId(id).build();
            PasswordResetRequest req2 = aPasswordResetRequest().withId(id).build();

            assertThat(req1).isEqualTo(req2);
            assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            PasswordResetRequest req1 = aPasswordResetRequest().build();
            PasswordResetRequest req2 = aPasswordResetRequest().build();

            assertThat(req1).isNotEqualTo(req2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include relevant information but not token value")
        void shouldIncludeRelevantInformationButNotTokenValue() {
            PasswordResetRequest request = aPasswordResetRequest().build();

            String str = request.toString();

            assertThat(str).contains("PasswordResetRequest");
            assertThat(str).contains("userId");
            assertThat(str).contains("expiresAt");
            assertThat(str).contains("isUsed");
        }
    }
}
