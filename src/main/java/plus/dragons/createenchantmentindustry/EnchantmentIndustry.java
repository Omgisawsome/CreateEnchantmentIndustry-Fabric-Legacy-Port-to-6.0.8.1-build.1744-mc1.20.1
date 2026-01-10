package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiTriggers;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class EnchantmentIndustry implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "create_enchantment_industry";
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

	@Override
	public void onInitialize() {
		CeiBlocks.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiFluids.register();
		CeiEntityTypes.register();
		CeiItems.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		CeiConfigs.register();
		REGISTRATE.register();

		CeiPackets.registerPackets();
		CeiAdvancements.register();
		CeiTriggers.register();
	}

	public static ResourceLocation genRL(String name) {
		return new ResourceLocation(MOD_ID, name);
	}
}
