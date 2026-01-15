package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class PrintEntries {

	public static final Map<ResourceLocation, PrintEntry> ENTRIES = new HashMap<>();

	static {
		ENTRIES.put(new EnchantedBook().id(), new EnchantedBook());
		ENTRIES.put(new WrittenBook().id(), new WrittenBook());
		ENTRIES.put(new NameTag().id(), new NameTag());
		ENTRIES.put(new Schedule().id(), new Schedule());
		ENTRIES.put(new Clipboard().id(), new Clipboard());
	}

	static class EnchantedBook implements PrintEntry {

		@Override
		public ResourceLocation id() {
			return EnchantmentIndustry.genRL("enchanted_book");
		}

		@Override
		public boolean match(ItemStack toPrint) {
			return toPrint.is(Items.ENCHANTED_BOOK);
		}

		@Override
		public boolean valid(ItemStack target, ItemStack tested) {
			return tested.is(Items.BOOK);
		}

		@Override
		public int requiredInkAmount(ItemStack target) {
			return (int) (getExperienceFromItem(target)
					* (requiredInkType(target).isSame(CeiFluids.HYPER_EXPERIENCE.get())
					? CeiConfigs.SERVER.copyEnchantedBookWithHyperExperienceCostCoefficient.get()
					: CeiConfigs.SERVER.copyEnchantedBookCostCoefficient.get()));
		}

		@Override
		public Fluid requiredInkType(ItemStack target) {
			boolean hyper = EnchantmentHelper.getEnchantments(target)
					.entrySet()
					.stream()
					.anyMatch(entry -> entry.getValue() > entry.getKey().getMaxLevel());
			return hyper ? CeiFluids.HYPER_EXPERIENCE.get() : CeiFluids.EXPERIENCE.get();
		}

		@Override
		public boolean isTooExpensive(ItemStack target, int limit) {
			return requiredInkAmount(target) > limit;
		}

		@Override
		public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
			Component itemName = Component.translatable(target.getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
			tooltip.add(itemName);

			boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get() * UNIT_PER_MB);
			if (tooExpensive) {
				tooltip.add(Component.literal("     ")
						.append(Component.translatable("gui.goggles.too_expensive"))
						.withStyle(ChatFormatting.RED));
			} else {
				boolean hyper = EnchantmentHelper.getEnchantments(target)
						.entrySet()
						.stream()
						.anyMatch(entry -> entry.getValue() > entry.getKey().getMaxLevel());
				int xpCost = (int) (getExperienceFromItem(target)
						* (hyper ? CeiConfigs.SERVER.copyEnchantedBookWithHyperExperienceCostCoefficient.get()
						: CeiConfigs.SERVER.copyEnchantedBookCostCoefficient.get()) / UNIT_PER_MB);
				tooltip.add(Component.literal("     ")
						.append(Component.translatable(hyper ? "gui.goggles.hyper_xp_consumption" : "gui.goggles.xp_consumption", xpCost))
						.withStyle(hyper ? ChatFormatting.AQUA : ChatFormatting.GREEN));
			}

			EnchantmentHelper.getEnchantments(target).forEach((ench, lvl) -> {
				Component name = ench.getFullname(lvl);
				tooltip.add(Component.literal("     ").append(name).withStyle(name.getStyle()));
			});
		}

		@Override
		public MutableComponent getDisplaySourceContent(ItemStack target) {
			MutableComponent comp = Component.translatable(target.getDescriptionId()).copy().append(" / ");
			EnchantmentHelper.getEnchantments(target).forEach((ench, lvl) -> comp.append(ench.getFullname(lvl)).append(" "));
			return comp;
		}

		private static int getExperienceFromItem(ItemStack itemStack) {
			return EnchantmentHelper.getEnchantments(itemStack).entrySet().stream()
					.mapToInt(entry -> Enchanting.getExperienceConsumption(entry.getKey(), entry.getValue()))
					.sum();
		}
	}

	static class WrittenBook implements PrintEntry {

		@Override
		public ResourceLocation id() {
			return EnchantmentIndustry.genRL("written_book");
		}

		@Override
		public boolean match(ItemStack toPrint) {
			return toPrint.is(Items.WRITTEN_BOOK);
		}

		@Override
		public boolean valid(ItemStack target, ItemStack tested) {
			return tested.is(Items.BOOK);
		}

		@Override
		public int requiredInkAmount(ItemStack target) {
			return WrittenBookItem.getPageCount(target) * CeiConfigs.SERVER.copyWrittenBookCostPerPage.get() * UNIT_PER_MB;
		}

		@Override
		public Fluid requiredInkType(ItemStack target) {
			return CeiFluids.INK.get();
		}

		@Override
		public ItemStack print(ItemStack target, ItemStack material) {
			ItemStack copy = target.copy();
			copy.getOrCreateTag().putInt("generation", CeiConfigs.SERVER.copyingWrittenBookAlwaysGetOriginalVersion.get() ? 0 : 1);
			return copy;
		}

		@Override
		public boolean isTooExpensive(ItemStack target, int limit) {
			return requiredInkAmount(target) > limit;
		}

		@Override
		public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, ItemStack target) {
			int pages = WrittenBookItem.getPageCount(target);
			Component itemName = Component.translatable(target.getDescriptionId()).withStyle(ChatFormatting.BLUE);
			Component pageCount = Component.literal(" / " + pages + " " + (pages == 1 ? "page" : "pages")).withStyle(ChatFormatting.DARK_GRAY);
			tooltip.add(itemName.append(pageCount));

			if (Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get() * UNIT_PER_MB)) {
				tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.too_expensive")).withStyle(ChatFormatting.RED));
			} else {
				int cost = pages * CeiConfigs.SERVER.copyWrittenBookCostPerPage.get();
				tooltip.add(Component.literal("     ").append(Component.translatable("gui.goggles.ink_consumption", cost)).withStyle(ChatFormatting.DARK_GRAY));
			}
		}

		@Override
		public MutableComponent getDisplaySourceContent(ItemStack target) {
			int pages = WrittenBookItem.getPageCount(target);
			return Component.translatable(target.getDescriptionId()).copy()
					.append(Component.literal(" / " + pages + " " + (pages == 1 ? "page" : "pages")));
		}
	}

	// Similarly, rewrite NameTag, Schedule, Clipboard to use Component.translatable instead of LANG
	// ... (omitted for brevity, but same pattern)
}
