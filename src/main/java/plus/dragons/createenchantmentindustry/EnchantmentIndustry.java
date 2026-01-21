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
	// Standardizing to ID to fix "cannot find symbol" errors in other classes
	public static final String ID = "create_enchantment_industry";
	public static final String MOD_ID = ID;
	public static final Logger LOGGER = LogManager.getLogger(ID);

	// Registrate instance for standard Create-style registration
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	// Fabric 1.20.1 uses 81 units per mB for fluid consistency
	public static final int UNIT_PER_MB = 81;

	@Override
	public void onInitialize() {
		// 1. Configs first
		CeiConfigs.register();

		// 2. Content registration
		// Note: CeiFluids.register() now uses vanilla registry to bypass Porting Lib issues
		CeiBlocks.register();
		CeiItems.register(); // Items usually before BlockEntities
		CeiFluids.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiEntityTypes.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		// 3. Finalize Registrate - This handles Blocks/Items/etc.
		REGISTRATE.register();

		// 4. Networking, Advancements, and Post-Registration logic
		CeiPackets.registerPackets();
		CeiAdvancements.register();

		LOGGER.info("Create: Enchantment Industry initialized successfully!");
	}

	public static ResourceLocation genRL(String path) {
		return new ResourceLocation(ID, path);
	}
}
