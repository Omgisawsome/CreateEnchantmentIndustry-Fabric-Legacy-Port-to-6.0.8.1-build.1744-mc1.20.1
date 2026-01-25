package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class PrinterDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (context.getSourceBlockEntity() instanceof PrinterBlockEntity printer) {
			if (printer.getCopyTarget().isEmpty()) {
				return Component.translatable("create_enchantment_industry.gui.goggles.printer.no_target");
			}

			long requiredAmount = Printing.getRequiredAmountForItem(printer.getCopyTarget());
			if (requiredAmount > 0) {
				// Convert droplets to mB for display (81 droplets = 1 mB)
				long mbAmount = requiredAmount / EnchantmentIndustry.UNIT_PER_MB;
				return Component.translatable("create_enchantment_industry.gui.goggles.printer.cost", mbAmount);
			}
		}
		return Component.empty();
	}

	@Override
	public boolean allowsLabeling(DisplayLinkContext context) {
		return false;
	}

	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return "Instant";
	}

	@Override
	protected String getTranslationKey() {
		return "printer_source";
	}
}
