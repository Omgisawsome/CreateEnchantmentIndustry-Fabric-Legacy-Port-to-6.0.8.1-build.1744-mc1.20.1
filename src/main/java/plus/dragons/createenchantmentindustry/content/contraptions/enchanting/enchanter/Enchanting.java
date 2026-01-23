package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class Enchanting {

	public static class Pair<F, S> {
		private final F first;
		private final S second;

		private Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}

		public static <F, S> Pair<F, S> of(F first, S second) {
			return new Pair<>(first, second);
		}

		public F getFirst() { return first; }
		public S getSecond() { return second; }
	}

	@Nullable
	public static EnchantmentEntry getTargetEnchantment(ItemStack itemStack, boolean hyper) {
		if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
			var result = EnchantmentGuideItem.getEnchantment(itemStack);
			if (result == null) return null;
			if (!hyper) return result;

			// Hyper-enchanting logic: Add +1 level
			return EnchantmentEntry.of(result.getFirst(), result.getSecond() + 1);
		}
		return null;
	}

	@Nullable
	public static EnchantmentEntry getValidEnchantment(ItemStack itemStack, ItemStack targetItem, boolean hyper) {
		var entry = getTargetEnchantment(targetItem, hyper);
		if (entry == null || !entry.valid())
			return null;

		Enchantment enchantment = entry.getFirst();
		int targetLevel = entry.getSecond();

		// 1. Check if the item already has this enchantment at an equal or higher level
		int currentLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
		if (currentLevel >= targetLevel) {
			return null;
		}

		// 2. Compatibility Check
		// We create a mutable copy of the enchantments to simulate the result
		Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(itemStack);

		// Verify compatibility with existing enchantments on the item
		for (Enchantment existing : existingEnchantments.keySet()) {
			if (existing != enchantment && !existing.isCompatibleWith(enchantment)) {
				return null;
			}
		}

		// 3. Item Validity
		// Note: Books are handled specifically by Blaze Enchanter logic in the BE
		if (!enchantment.canEnchant(itemStack) && !itemStack.is(net.minecraft.world.item.Items.BOOK)) {
			// If it's not a book and the enchantment can't go on this item, fail.
			// Exception: If the item is already enchanted with it (checked above), we allow upgrading.
			if (currentLevel == 0) return null;
		}

		return entry;
	}

	public static void enchantItem(ItemStack itemStack, Pair<Enchantment, Integer> enchantment) {
		Map<Enchantment, Integer> map = new HashMap<>(EnchantmentHelper.getEnchantments(itemStack));
		map.put(enchantment.getFirst(), enchantment.getSecond());
		EnchantmentHelper.setEnchantments(map, itemStack);
	}

	// Experience math for 1.20.1
	public static int expPointFromLevel(int level) {
		if (level >= 31) return (int) (4.5 * level * level - 162.5 * level + 2220);
		if (level >= 16) return (int) (2.5 * level * level - 40.5 * level + 360);
		return level * level + 6 * level;
	}

	public static int expPointForNextLevel(int level) {
		if (level >= 30) return 9 * level - 158;
		if (level >= 15) return 5 * level - 38;
		return 2 * level + 7;
	}

	public static int getExperienceConsumption(Enchantment enchantment, int level) {
		// Calculate a base cost based on rarity and level
		// Rarity values: Common(10), Uncommon(5), Rare(2), Very Rare(1)
		// We invert this for cost calculation
		int weight = switch (enchantment.getRarity()) {
			case COMMON -> 1;
			case UNCOMMON -> 2;
			case RARE -> 4;
			case VERY_RARE -> 8;
		};

		int cost = (enchantment.getMinCost(level) + (level * weight));
		return cost * (int) UNIT_PER_MB;
	}
}
