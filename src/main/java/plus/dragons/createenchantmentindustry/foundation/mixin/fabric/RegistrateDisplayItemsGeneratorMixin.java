package plus.dragons.createenchantmentindustry.foundation.mixin.fabric;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

@Deprecated
@Mixin(targets = "com.simibubi.create.AllCreativeModeTabs$RegistrateDisplayItemsGenerator", remap = false)
public abstract class RegistrateDisplayItemsGeneratorMixin {

	@Shadow @Final private Supplier<CreativeModeTab> tabFilter;

	@SuppressWarnings("unchecked")
	private static void addOrdering(List<Object> orderings, String name, Item item, Item anchor) {
		try {
			Class<?> cls = Class.forName("com.simibubi.create.AllCreativeModeTabs$RegistrateDisplayItemsGenerator$ItemOrdering");
			Method method = cls.getMethod(name, Item.class, Item.class);
			Object ordering = method.invoke(null, item, anchor);
			orderings.add(ordering);
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject(method = "makeOrderings", at = @At("TAIL"), cancellable = true)
	private static void injectMakeOrderingsReturn(CallbackInfoReturnable<List<?>> cir) {
		List<?> orderings = cir.getReturnValue();

		Map<Item, Item> afterOrderings = Map.of(
				CeiBlocks.DISENCHANTER.asItem(), AllBlocks.ITEM_DRAIN.asItem(),
				CeiBlocks.PRINTER.asItem(), AllBlocks.SPOUT.asItem(),
				CeiItems.ENCHANTING_GUIDE.asItem(), AllBlocks.BLAZE_BURNER.asItem(),
				CeiItems.HYPER_EXP_BOTTLE.asItem(), AllItems.SUPER_GLUE.asItem(),
				AllFluids.HONEY.get().getBucket().asItem(), CeiFluids.INK.get().getBucket().asItem(),
				AllFluids.CHOCOLATE.get().getBucket().asItem(), CeiFluids.INK.get().getBucket().asItem()
		);

		afterOrderings.forEach((item, anchor) -> addOrdering((List<Object>) orderings, "after", item, anchor));

		cir.setReturnValue(orderings);
	}

	@Inject(method = "collectBlocks", at = @At("TAIL"), cancellable = true)
	private void injectCollectBlocks(Predicate<Item> exclusionPredicate, CallbackInfoReturnable<List<Item>> cir) {
		List<Item> items = cir.getReturnValue();
		CreativeModeTab tab = tabFilter.get();
		var tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);

		for (RegistryEntry<Block> entry : REGISTRATE.getAll(Registries.BLOCK)) {
			if (tabKey != null && !CreateRegistrate.isInCreativeTab(entry, tabKey))
				continue;
			Block block = entry.get();
			Item item = block.asItem();
			if (item == Items.AIR || exclusionPredicate.test(item))
				continue;
			items.add(item);
		}

		cir.setReturnValue(new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items)));
	}

	@Inject(method = "collectItems", at = @At("TAIL"), cancellable = true)
	private void injectCollectItems(Predicate<Item> exclusionPredicate, CallbackInfoReturnable<List<Item>> cir) {
		List<Item> items = cir.getReturnValue();
		CreativeModeTab tab = tabFilter.get();
		var tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);

		for (RegistryEntry<Item> entry : REGISTRATE.getAll(Registries.ITEM)) {
			if (tabKey != null && !CreateRegistrate.isInCreativeTab(entry, tabKey))
				continue;
			Item item = entry.get();
			if (item instanceof BlockItem || exclusionPredicate.test(item))
				continue;
			items.add(item);
		}

		cir.setReturnValue(new ReferenceArrayList<>(items));
	}
}
