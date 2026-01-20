package plus.dragons.createenchantmentindustry.entry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import java.util.List; // Added import

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.TargetEnchantmentDisplaySource;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterDisplaySource;

public class CeiBlocks {

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key());
	}

	public static final BlockEntry<DisenchanterBlock> DISENCHANTER = REGISTRATE
			.block("disenchanter", DisenchanterBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
			.addLayer(() -> RenderType::cutoutMipped)
			.simpleItem()
			.register();

	public static final BlockEntry<PrinterBlock> PRINTER = REGISTRATE
			.block("printer", PrinterBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.onRegister(assignDataBehaviour(new PrinterDisplaySource(), "copy_content"))
			.blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.partialBaseModel(ctx, pov)))
			.addLayer(() -> RenderType::cutoutMipped)
			.item(AssemblyOperatorBlockItem::new)
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final BlockEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = REGISTRATE
			.block("blaze_enchanter", BlazeEnchanterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel(BlazeEnchanterBlock::getLight))
			.onRegister(assignDataBehaviour(new TargetEnchantmentDisplaySource(), "target_enchantment"))
			.addLayer(() -> RenderType::cutoutMipped)
			.blockstate((ctx, pov) -> pov.simpleBlock(ctx.get(), AssetLookup.standardModel(ctx, pov)))
			.register();

	/**
	 * FIXED HELPER: Wraps the source in a List to satisfy the Create 0.6.x API.
	 */
	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplaySource source, String id) {
		return block -> DisplaySource.BY_BLOCK.register(block, List.of(source));
	}

	public static void register() {}

}
