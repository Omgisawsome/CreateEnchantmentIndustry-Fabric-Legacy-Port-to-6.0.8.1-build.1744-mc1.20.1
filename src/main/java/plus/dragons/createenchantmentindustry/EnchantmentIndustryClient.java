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

		// FABRIC/CATNIP FIX:
		// In Create 0.6.x Fabric ports, 'withSpecs' is the most stable method.
		// It expects (Client, Common, Server). We pass null for the ones we don't have.
		BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.MOD_ID, (BaseConfigScreen screen) -> {
			return screen.withSpecs(null, null, CeiConfigs.server().specification);
		});

		ClientEvents.register();
	}
}
