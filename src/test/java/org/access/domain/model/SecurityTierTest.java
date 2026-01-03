package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityTier Enum")
class SecurityTierTest {

    @Nested
    @DisplayName("Tier levels")
    class TierLevels {

        @Test
        @DisplayName("TIER_0 should have level 0 (highest privilege)")
        void tier0ShouldHaveLevel0() {
            assertThat(SecurityTier.TIER_0.level()).isEqualTo(0);
            assertThat(SecurityTier.TIER_0.description()).isEqualTo("Critical");
        }

        @Test
        @DisplayName("TIER_1 should have level 1 (sensitive)")
        void tier1ShouldHaveLevel1() {
            assertThat(SecurityTier.TIER_1.level()).isEqualTo(1);
            assertThat(SecurityTier.TIER_1.description()).isEqualTo("Sensitive");
        }

        @Test
        @DisplayName("TIER_2 should have level 2 (standard)")
        void tier2ShouldHaveLevel2() {
            assertThat(SecurityTier.TIER_2.level()).isEqualTo(2);
            assertThat(SecurityTier.TIER_2.description()).isEqualTo("Standard");
        }

        @Test
        @DisplayName("defaultTier should return TIER_2")
        void defaultTierShouldReturnTier2() {
            assertThat(SecurityTier.defaultTier()).isEqualTo(SecurityTier.TIER_2);
        }
    }

    @Nested
    @DisplayName("isAtLeast comparison")
    class IsAtLeast {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_0, true",
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, true",
            "TIER_1, TIER_2, true",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, true"
        })
        @DisplayName("should correctly compare tier privileges")
        void shouldCorrectlyCompareTierPrivileges(SecurityTier actor, SecurityTier target, boolean expected) {
            assertThat(actor.isAtLeast(target)).isEqualTo(expected);
        }

        @Test
        @DisplayName("TIER_0 should be at least all tiers")
        void tier0ShouldBeAtLeastAllTiers() {
            assertThat(SecurityTier.TIER_0.isAtLeast(SecurityTier.TIER_0)).isTrue();
            assertThat(SecurityTier.TIER_0.isAtLeast(SecurityTier.TIER_1)).isTrue();
            assertThat(SecurityTier.TIER_0.isAtLeast(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_2 should only be at least TIER_2")
        void tier2ShouldOnlyBeAtLeastTier2() {
            assertThat(SecurityTier.TIER_2.isAtLeast(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_2.isAtLeast(SecurityTier.TIER_1)).isFalse();
            assertThat(SecurityTier.TIER_2.isAtLeast(SecurityTier.TIER_2)).isTrue();
        }
    }

    @Nested
    @DisplayName("isHigherThan comparison")
    class IsHigherThan {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_0, false",
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, false",
            "TIER_1, TIER_2, true",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, false"
        })
        @DisplayName("should correctly compare strict tier privileges")
        void shouldCorrectlyCompareStrictTierPrivileges(SecurityTier actor, SecurityTier target, boolean expected) {
            assertThat(actor.isHigherThan(target)).isEqualTo(expected);
        }

        @Test
        @DisplayName("TIER_0 should be higher than TIER_1 and TIER_2")
        void tier0ShouldBeHigherThanLowerTiers() {
            assertThat(SecurityTier.TIER_0.isHigherThan(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_0.isHigherThan(SecurityTier.TIER_1)).isTrue();
            assertThat(SecurityTier.TIER_0.isHigherThan(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_2 should not be higher than any tier")
        void tier2ShouldNotBeHigherThanAnyTier() {
            assertThat(SecurityTier.TIER_2.isHigherThan(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_2.isHigherThan(SecurityTier.TIER_1)).isFalse();
            assertThat(SecurityTier.TIER_2.isHigherThan(SecurityTier.TIER_2)).isFalse();
        }
    }

    @Nested
    @DisplayName("canManage comparison")
    class CanManage {

        @ParameterizedTest
        @CsvSource({
            "TIER_0, TIER_0, false",
            "TIER_0, TIER_1, true",
            "TIER_0, TIER_2, true",
            "TIER_1, TIER_0, false",
            "TIER_1, TIER_1, false",
            "TIER_1, TIER_2, true",
            "TIER_2, TIER_0, false",
            "TIER_2, TIER_1, false",
            "TIER_2, TIER_2, false"
        })
        @DisplayName("should correctly determine if actor can manage target tier")
        void shouldCorrectlyDetermineIfActorCanManageTargetTier(SecurityTier actor, SecurityTier target, boolean expected) {
            assertThat(actor.canManage(target)).isEqualTo(expected);
        }

        @Test
        @DisplayName("TIER_0 can manage TIER_1 and TIER_2 but not TIER_0")
        void tier0CanManageLowerTiersButNotItself() {
            assertThat(SecurityTier.TIER_0.canManage(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_0.canManage(SecurityTier.TIER_1)).isTrue();
            assertThat(SecurityTier.TIER_0.canManage(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_1 can only manage TIER_2")
        void tier1CanOnlyManageTier2() {
            assertThat(SecurityTier.TIER_1.canManage(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_1.canManage(SecurityTier.TIER_1)).isFalse();
            assertThat(SecurityTier.TIER_1.canManage(SecurityTier.TIER_2)).isTrue();
        }

        @Test
        @DisplayName("TIER_2 cannot manage any tier")
        void tier2CannotManageAnyTier() {
            assertThat(SecurityTier.TIER_2.canManage(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_2.canManage(SecurityTier.TIER_1)).isFalse();
            assertThat(SecurityTier.TIER_2.canManage(SecurityTier.TIER_2)).isFalse();
        }
    }

    @Nested
    @DisplayName("Privilege escalation prevention")
    class PrivilegeEscalationPrevention {

        @Test
        @DisplayName("no tier can manage itself - prevents self-escalation")
        void noTierCanManageItself() {
            for (SecurityTier tier : SecurityTier.values()) {
                assertThat(tier.canManage(tier))
                    .as("Tier %s should not be able to manage itself", tier)
                    .isFalse();
            }
        }

        @Test
        @DisplayName("lower privilege tiers cannot manage higher privilege tiers")
        void lowerPrivilegeCannotManageHigherPrivilege() {
            assertThat(SecurityTier.TIER_2.canManage(SecurityTier.TIER_1)).isFalse();
            assertThat(SecurityTier.TIER_2.canManage(SecurityTier.TIER_0)).isFalse();
            assertThat(SecurityTier.TIER_1.canManage(SecurityTier.TIER_0)).isFalse();
        }
    }
}
