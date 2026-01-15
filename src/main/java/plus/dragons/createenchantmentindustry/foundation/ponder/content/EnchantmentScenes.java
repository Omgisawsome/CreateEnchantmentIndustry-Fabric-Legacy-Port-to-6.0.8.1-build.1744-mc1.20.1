package plus.dragons.createenchantmentindustry.foundation.ponder.content;

/**
 * Ponder scenes are intentionally disabled during Fabric 1.20.1 porting.
 *
 * Reason:
 * - Create Ponder is an optional module
 * - It is not present on the Fabric compile classpath by default
 * - Including it causes hard compilation failures
 *
 * Gameplay is NOT affected.
 * These methods are kept as no-ops to preserve references.
 *
 * Ponder can be reintroduced later once the core port is stable.
 */
public class EnchantmentScenes {

	public static void disenchant(Object scene, Object util) {
	}

	public static void transformBlazeBurner(Object scene, Object util) {
	}

	public static void enchant(Object scene, Object util) {
	}

	public static void hyperEnchant(Object scene, Object util) {
	}

	public static void handleExperienceNugget(Object scene, Object util) {
	}

	public static void dropExperienceNugget(Object scene, Object util) {
	}

	public static void crushingWheelTweak(Object scene, Object util) {
	}

	public static void handleExperienceBottle(Object scene, Object util) {
	}

	public static void copy(Object scene, Object util) {
	}

	public static void leak(Object scene, Object util) {
	}
}
