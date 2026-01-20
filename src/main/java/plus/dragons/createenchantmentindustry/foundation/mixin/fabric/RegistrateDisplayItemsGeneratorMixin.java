package plus.dragons.createenchantmentindustry.foundation.mixin.fabric;

import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.util.List;
import java.util.function.Predicate;

@Mixin(targets = "com.simibubi.create.AllCreativeModeTabs$RegistrateDisplayItemsGenerator", remap = false)
public abstract class RegistrateDisplayItemsGeneratorMixin {

	/**
	 * CRITICAL FIX: The previous version was empty.
	 * We must add our mod's items to the 'orderings' list so Create's
	 * generator knows how to sort them, preventing the ClassCastException.
	 */
	@Inject(method = "makeOrderings", at = @At("RETURN"), cancellable = true)
	private static void ce_injectMakeOrderings(CallbackInfoReturnable<List<Object>> cir) {
		List<Object> orderings = cir.getReturnValue();
		// Add all items from this mod to the ordering list used by the generator
		orderings.addAll(EnchantmentIndustry.REGISTRATE.getAll(Registries.ITEM));
	}

	@Inject(method = "collectBlocks", at = @At("RETURN"), cancellable = true)
	private void ce_injectCollectBlocks(Predicate<Item> exclusionPredicate, CallbackInfoReturnable<List<Item>> cir) {
		List<Item> items = cir.getReturnValue();
		for (RegistryEntry<Block> entry : EnchantmentIndustry.REGISTRATE.getAll(Registries.BLOCK)) {
			Block block = entry.get();
			Item item = block.asItem();
			if (item != Items.AIR && !exclusionPredicate.test(item) && !items.contains(item)) {
				items.add(item);
			}
		}
	}

	@Inject(method = "collectItems", at = @At("RETURN"), cancellable = true)
	private void ce_injectCollectItems(Predicate<Item> exclusionPredicate, CallbackInfoReturnable<List<Item>> cir) {
		List<Item> items = cir.getReturnValue();
		for (RegistryEntry<Item> entry : EnchantmentIndustry.REGISTRATE.getAll(Registries.ITEM)) {
			Item item = entry.get();
			// Filter out BlockItems (handled above) and apply the exclusion predicate
			if (!(item instanceof BlockItem) && !exclusionPredicate.test(item) && !items.contains(item)) {
				items.add(item);
			}
		}
	}
}
