package plus.dragons.createenchantmentindustry.entry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiCreativeModeTabs {

	public static final CreativeModeTab BASE_CREATIVE_TAB = FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.create_enchantment_industry.main"))
			.icon(() -> new ItemStack(CeiBlocks.DISENCHANTER.get()))
			.displayItems((parameters, output) -> {
				// Automatically add every item registered by your Registrate to this tab
				EnchantmentIndustry.REGISTRATE.getAll(Registries.ITEM).stream()
						.forEach(entry -> output.accept(entry.get()));
			})
			.build();

	public static void register() {
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				EnchantmentIndustry.genRL("main"),
				BASE_CREATIVE_TAB
		);
	}
}
