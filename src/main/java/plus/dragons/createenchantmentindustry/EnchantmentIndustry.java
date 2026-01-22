package plus.dragons.createenchantmentindustry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
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

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	public static final int UNIT_PER_MB = 81;

	@Override
	public void onInitialize() {
		// 1. Configs and Triggers
		CeiConfigs.register();
		CeiTriggers.register();

		// 2. Content registration
		CeiBlocks.register();
		CeiItems.register();
		CeiFluids.register();
		CeiBlockEntities.register();
		CeiContainerTypes.register();
		CeiEntityTypes.register();
		CeiRecipeTypes.register();
		CeiTags.register();

		// 3. Finalize Registrate
		REGISTRATE.register();

		// 4. Manual Tab Injection
		ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()).register(content -> {
			content.accept(CeiItems.ENCHANTING_GUIDE.get());
			content.accept(CeiItems.HYPER_EXP_BOTTLE.get());
			content.accept(CeiBlocks.DISENCHANTER.get());
			content.accept(CeiBlocks.PRINTER.get());
			content.accept(CeiBlocks.BLAZE_ENCHANTER.get());
		});

		// 5. FLUID STORAGE REGISTRATION - FIXES FILTERS
		// Register Standard Experience Bottle
		FluidStorage.ITEM.registerForItems((stack, context) ->
						new FixedBottleStorage(context, FluidVariant.of(CeiFluids.EXPERIENCE)),
				Items.EXPERIENCE_BOTTLE
		);

		// Register Hyper Experience Bottle
		FluidStorage.ITEM.registerForItems((stack, context) ->
						new FixedBottleStorage(context, FluidVariant.of(CeiFluids.HYPER_EXPERIENCE)),
				CeiItems.HYPER_EXP_BOTTLE.get()
		);

		// 6. Secondary Systems
		CeiAdvancements.register();
		CeiPackets.registerPackets();

		LOGGER.info("Create: Enchantment Industry initialized successfully!");
	}

	/**
	 * A version-independent implementation of a fixed fluid storage for bottles.
	 * This allows Smart Pipes to "see" fluid inside items that aren't buckets.
	 */
	private static class FixedBottleStorage extends SingleVariantItemStorage<FluidVariant> {
		private final FluidVariant fluid;

		public FixedBottleStorage(ContainerItemContext context, FluidVariant fluid) {
			super(context);
			this.fluid = fluid;
		}

		// Changed from getBlankVariant to getBlankResource to match your API build
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
			// Returns the same item because we are only using this for filter detection
			return itemVariant;
		}
	}

	public static ResourceLocation genRL(String path) {
		return new ResourceLocation(ID, path);
	}
}
