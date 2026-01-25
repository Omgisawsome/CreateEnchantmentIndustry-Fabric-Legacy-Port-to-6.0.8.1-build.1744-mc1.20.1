package plus.dragons.createenchantmentindustry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.dragons.createenchantmentindustry.entry.*;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiTriggers;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class EnchantmentIndustry implements ModInitializer {
	public static final String ID = "create_enchantment_industry";
	public static final String MOD_ID = ID;
	public static final Logger LOGGER = LogManager.getLogger(ID);

	// KEY FIX: Initialize REGISTRATE with the tab setting in one go.
	// This ResourceKey must match the one used in CeiCreativeModeTabs.
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
			.setCreativeTab(ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(ID, "main")));

	public static final int UNIT_PER_MB = 81;

	@Override
	public void onInitialize() {
		// 1. Register the Creative Tab FIRST.
		// This ensures the ResourceKey used above actually points to a valid tab.
		CeiCreativeModeTabs.register();

		// 2. Configs and Triggers
		CeiConfigs.register();
		CeiTriggers.register();

		// 3. Register Content
		// Since REGISTRATE is already configured with .setCreativeTab(),
		// these items will automatically go to your custom tab.
		CeiBlocks.register();
		CeiItems.register();
		CeiFluids.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiEntityTypes.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		// 4. Finalize Registrate
		REGISTRATE.register();

		// 5. Fluid Storage Registration
		FluidStorage.ITEM.registerForItems((stack, context) ->
						new FixedBottleStorage(context, FluidVariant.of(CeiFluids.EXPERIENCE)),
				Items.EXPERIENCE_BOTTLE
		);

		FluidStorage.ITEM.registerForItems((stack, context) ->
						new FixedBottleStorage(context, FluidVariant.of(CeiFluids.HYPER_EXPERIENCE)),
				CeiItems.HYPER_EXP_BOTTLE.get()
		);

		// 6. Secondary Systems
		CeiAdvancements.register();
		CeiPackets.registerPackets();

		LOGGER.info("Create: Enchantment Industry initialized successfully!");
	}

	private static class FixedBottleStorage extends SingleVariantItemStorage<FluidVariant> {
		private final FluidVariant fluid;

		public FixedBottleStorage(ContainerItemContext context, FluidVariant fluid) {
			super(context);
			this.fluid = fluid;
		}

		@Override
		protected FluidVariant getBlankResource() {
			return FluidVariant.blank();
		}

		@Override
		protected long getCapacity(FluidVariant variant) {
			return 250L * UNIT_PER_MB;
		}

		@Override
		protected FluidVariant getResource(ItemVariant itemVariant) {
			return fluid;
		}

		@Override
		protected long getAmount(ItemVariant itemVariant) {
			return 250L * UNIT_PER_MB;
		}

		@Override
		protected ItemVariant getUpdatedVariant(ItemVariant itemVariant, FluidVariant fluidVariant, long amount) {
			return itemVariant;
		}
	}

	public static ResourceLocation genRL(String path) {
		return new ResourceLocation(ID, path);
	}
}
