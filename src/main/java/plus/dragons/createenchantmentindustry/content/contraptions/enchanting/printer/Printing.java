package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiTags;

public class Printing {

	public static boolean isValid(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.is(Items.BOOK)) return true;
		if (stack.is(Items.NAME_TAG) && stack.hasCustomHoverName()) return true;
		if (stack.is(Items.WRITTEN_BOOK)) return true;
		if (stack.is(Items.ENCHANTED_BOOK)) return true;

		// FIXED: Use the matches() method from CeiTags enum
		return CeiTags.ItemTag.PRINTER_INPUT.matches(stack);
	}

	public static boolean isTooExpensive(ItemStack target, int limit) {
		long amount = getRequiredAmountForItem(target);
		return amount > (long) limit * EnchantmentIndustry.UNIT_PER_MB;
	}

	public static FluidVariant getRequiredFluidForItem(ItemStack target) {
		if (target.is(Items.WRITTEN_BOOK)) return FluidVariant.of(CeiFluids.INK);
		if (target.is(Items.ENCHANTED_BOOK)) return FluidVariant.of(CeiFluids.EXPERIENCE);
		if (target.is(Items.NAME_TAG)) return FluidVariant.of(CeiFluids.EXPERIENCE);
		return null;
	}

	public static long getRequiredAmountForItem(ItemStack target) {
		if (target.is(Items.WRITTEN_BOOK))
			return 5L * EnchantmentIndustry.UNIT_PER_MB;
		if (target.is(Items.NAME_TAG))
			return 7L * EnchantmentIndustry.UNIT_PER_MB;
		if (target.is(Items.ENCHANTED_BOOK)) {
			return (long) getExperienceFromItem(target) * EnchantmentIndustry.UNIT_PER_MB;
		}
		return -1;
	}

	private static int getExperienceFromItem(ItemStack stack) {
		return EnchantmentHelper.getEnchantments(stack).entrySet().stream()
				.mapToInt(e -> e.getKey().getMinCost(e.getValue()))
				.sum();
	}

	public static ItemStack process(ItemStack target, ItemStack input, Storage<FluidVariant> fluidStorage) {
		FluidVariant requiredFluid = getRequiredFluidForItem(target);
		long requiredAmount = getRequiredAmountForItem(target);

		if (requiredFluid == null || requiredAmount <= 0) return null;

		try (Transaction t = Transaction.openOuter()) {
			long extracted = fluidStorage.extract(requiredFluid, requiredAmount, t);
			if (extracted == requiredAmount) {
				t.commit();
				ItemStack result = target.copy();
				result.setCount(1);
				return result;
			}
		}
		return null;
	}

	public static ItemStack match(ItemStack stack) {
		return isValid(stack) ? stack : null;
	}
}
