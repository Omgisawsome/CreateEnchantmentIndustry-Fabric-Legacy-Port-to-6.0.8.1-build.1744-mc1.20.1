package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class EnchantmentIndustry implements ModInitializer {

	public static final String MOD_ID = "create_enchantment_industry";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

	// Fabric 1.20.1 uses 81 units per mB
	public static final int UNIT_PER_MB = 81;

	@Override
	public void onInitialize() {
		CeiConfigs.register();

		// Content registration
		CeiBlocks.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiFluids.register();
		CeiEntityTypes.register();
		CeiItems.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		// CRITICAL: Finalize registration. This MUST be called last.
		REGISTRATE.register();

		// Networking & Advancements
		CeiPackets.registerPackets();
		CeiAdvancements.register();

		LOGGER.info("Create Enchantment Industry initialized");
	}

	public static ResourceLocation genRL(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
