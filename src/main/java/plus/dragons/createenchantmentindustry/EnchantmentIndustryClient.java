package plus.dragons.createenchantmentindustry;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createenchantmentindustry.entry.CeiBlockPartials;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.foundation.events.ClientEvents;
import plus.dragons.createenchantmentindustry.foundation.ponder.content.CeiPonderIndex;

public class EnchantmentIndustryClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// 1. Load the custom block models (Blaze Enchanter, Printer)
		// CRITICAL: This prevents the "Purple/Black" texture on the blaze guy
		CeiBlockPartials.register();

		// 2. Load Ponder scenes (W-key tutorials)
		CeiPonderIndex.register();

		// 3. Register the Config Screen
		BaseConfigScreen.setDefaultActionFor(EnchantmentIndustry.MOD_ID, (BaseConfigScreen screen) -> {
			return screen.withSpecs(null, null, CeiConfigs.server().specification);
		});

		// 4. Register Client Events (Input handling, ticking)
		ClientEvents.register();

		// 5. Register Fluid Rendering (Experience, Ink)
		registerFluidRenderers();
	}

	private void registerFluidRenderers() {
		// Experience
		FluidRenderHandlerRegistry.INSTANCE.register(CeiFluids.EXPERIENCE, CeiFluids.FLOWING_EXPERIENCE,
				new SimpleFluidRenderHandler(
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/experience_still"),
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/experience_flow")
				));

		// Hyper Experience
		FluidRenderHandlerRegistry.INSTANCE.register(CeiFluids.HYPER_EXPERIENCE, CeiFluids.FLOWING_HYPER_EXPERIENCE,
				new SimpleFluidRenderHandler(
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/hyper_experience_still"),
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/hyper_experience_flow")
				));

		// Ink
		FluidRenderHandlerRegistry.INSTANCE.register(CeiFluids.INK, CeiFluids.FLOWING_INK,
				new SimpleFluidRenderHandler(
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/ink_still"),
						new ResourceLocation(EnchantmentIndustry.MOD_ID, "fluid/ink_flow")
				));
	}
}
