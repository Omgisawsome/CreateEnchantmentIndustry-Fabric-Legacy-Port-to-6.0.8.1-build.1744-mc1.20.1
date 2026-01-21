package plus.dragons.createenchantmentindustry.entry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import java.util.List;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.TargetEnchantmentDisplaySource;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterDisplaySource;

public class CeiBlocks {

	// FIXED: Moved tab setting to individual entries to avoid ClassCastException

	public static final BlockEntry<DisenchanterBlock> DISENCHANTER = REGISTRATE
			.block("disenchanter", DisenchanterBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			// FIXED: Manual blockstate to ensure textures point to Create's assets
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
					.withExistingParent(c.getName(), new ResourceLocation("block/cube_all"))
					.texture("all", new ResourceLocation("create", "block/copper_casing"))))
			.addLayer(() -> RenderType::cutoutMipped)
			.item()
			.tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key())
			.build()
			.register();

	public static final BlockEntry<PrinterBlock> PRINTER = REGISTRATE
			.block("printer", PrinterBlock::new)
			.initialProperties(SharedProperties::copperMetal)
			.onRegister(assignDataBehaviour(new PrinterDisplaySource(), "copy_content"))
			.blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
					.withExistingParent(c.getName(), new ResourceLocation("create", "block/printer/block")) // Use Create's internal parent if available
					.texture("copper", new ResourceLocation("create", "block/copper_casing"))))
			.addLayer(() -> RenderType::cutoutMipped)
			.item(AssemblyOperatorBlockItem::new)
			.tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key())
			.build()
			.register();

	public static final BlockEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = REGISTRATE
			.block("blaze_enchanter", BlazeEnchanterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.lightLevel(BlazeEnchanterBlock::getLight))
			.onRegister(assignDataBehaviour(new TargetEnchantmentDisplaySource(), "target_enchantment"))
			.addLayer(() -> RenderType::cutoutMipped)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
					.withExistingParent(c.getName(), new ResourceLocation("create", "block/blaze_burner/block_blaze"))))
			.register();

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplaySource source, String id) {
		return block -> DisplaySource.BY_BLOCK.register(block, List.of(source));
	}

	public static void register() {}
}
