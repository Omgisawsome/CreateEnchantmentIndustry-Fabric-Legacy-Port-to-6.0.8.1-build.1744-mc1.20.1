package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiPackets;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiTriggers;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class EnchantmentIndustry implements ModInitializer {

	/* ---------------------------- */
	/* Constants & Core References  */
	/* ---------------------------- */

	public static final String MOD_ID = "create_enchantment_industry";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

	/**
	 * Create uses 81 units per millibucket for experience fluid
	 * This constant is relied on by multiple block entities.
	 */
	public static final int UNIT_PER_MB = 81;

	/* ---------------------------- */
	/* Mod Initialization           */
	/* ---------------------------- */

	@Override
	public void onInitialize() {

		// --- Content registration ---
		CeiBlocks.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiFluids.register();
		CeiEntityTypes.register();
		CeiItems.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		// --- Config & registrate ---
		CeiConfigs.register();
		REGISTRATE.register();

		// --- Networking & Advancements ---
		CeiPackets.registerPackets();
		CeiAdvancements.register();
		CeiTriggers.register();

		LOGGER.info("Create Enchantment Industry initialized");
	}

	/* ---------------------------- */
	/* Helpers                      */
	/* ---------------------------- */

	public static ResourceLocation genRL(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
