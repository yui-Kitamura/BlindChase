package pro.eng.yui.mcpl.blindChase.game.listener;

import org.bukkit.entity.Player;

/**
 * Provides extensible per-player cooldown values for White Cane interactions.
 *
 * Default implementation returns 600ms (0.6s) and 12 client ticks.
 * Other plugins or components can replace the provider at runtime via {@link #setProvider(Provider)}
 * to implement permission-based, config-based, or dynamic cooldowns.
 */
public final class WhiteCaneCooldowns {

    private WhiteCaneCooldowns() { }

    /** Strategy interface for providing cooldown values. */
    public interface Provider {
        /**
         * Cooldown duration in milliseconds for the given player.
         */
        long getCooldownMillis(Player player);

        /**
         * Client visual cooldown (via Player#setCooldown) in ticks for the given player.
         * Return 0 or negative to skip calling setCooldown.
         */
        int getClientCooldownTicks(Player player);
    }

    private static volatile Provider provider = new DefaultProvider();

    /** Get the active provider (never null). */
    public static Provider getProvider() { return provider; }

    /**
     * Replace the provider. Passing null restores the default provider.
     */
    public static void setProvider(Provider newProvider) {
        provider = (newProvider == null) ? new DefaultProvider() : newProvider;
    }

    /** Default fixed values provider. */
    private static final class DefaultProvider implements Provider {
        private static final long DEFAULT_MS = 600L; // 0.6 seconds
        private static final int DEFAULT_TICKS = 12; // 12 ticks = 0.6s

        @Override
        public long getCooldownMillis(Player player) {
            return DEFAULT_MS;
        }

        @Override
        public int getClientCooldownTicks(Player player) {
            return DEFAULT_TICKS;
        }
    }
}
