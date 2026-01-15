package plus.dragons.createenchantmentindustry;

import net.fabricmc.api.ClientModInitializer;
// Corrected import for Create 0.6.x (Build 288)
// This class now lives in Catnip, NOT Create
import net.createmod.catnip.config.ui.BaseConfigScreen;

import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.events.ClientEvents;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

public class EnchantmentIndustryClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// --- Register Block Partials (Rendering) ---
		CeiBlockPartials.register();

		// --- Register Ponder Content ---
		CeiPonderIndex.register();
		CeiPonderIndex.registerTags();

		// --- Config Screen Integration ---
		// BaseConfigScreen is now handled by the Catnip library in 0.6.x
		// Ensure EnchantmentIndustry.MOD_ID is correctly defined in your main class
		BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.MOD_ID, screen -> screen
				.withTitles(null, null, "Gameplay Settings")
				.withSpecs(null, null, CeiConfigs.SERVER_SPEC)
		);

		// --- Register Client Events ---
		ClientEvents.register();
	}
}
