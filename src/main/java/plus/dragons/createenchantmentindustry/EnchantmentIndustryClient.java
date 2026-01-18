package plus.dragons.createenchantmentindustry;

import net.createmod.catnip.config.ui.BaseConfigScreen;

import net.fabricmc.api.ClientModInitializer;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.events.ClientEvents;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;
public class EnchantmentIndustryClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CeiBlockPartials.register();

		CeiPonderIndex.register();

		BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.ID, screen -> screen
				.withTitles(null, null, "Gameplay Settings")
				.withSpecs(null, null, CeiConfigs.SERVER_SPEC)
		);

		ClientEvents.register();
	}
}
