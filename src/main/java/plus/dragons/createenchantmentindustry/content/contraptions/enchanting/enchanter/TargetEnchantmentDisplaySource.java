package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class TargetEnchantmentDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof BlazeEnchanterBlockEntity blazeEnchanter))
			return EMPTY_LINE;

		// Using the same logic as the renderer/BE to get the enchantment data
		var entry = Enchanting.getValidEnchantment(blazeEnchanter.getHeldItemStack(), blazeEnchanter.targetItem, blazeEnchanter.hyper());

		// If no valid enchantment is found, return the "Invalid Target" translation
		if (entry == null) {
			return Component.translatable(EnchantmentIndustry.MOD_ID + ".gui.goggles.blaze_enchanter.invalid_target");
		}

		// Return the full name of the enchantment (e.g., "Sharpness V")
		return (MutableComponent) entry.getFirst().getFullname(entry.getSecond());
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return false;
	}
}
