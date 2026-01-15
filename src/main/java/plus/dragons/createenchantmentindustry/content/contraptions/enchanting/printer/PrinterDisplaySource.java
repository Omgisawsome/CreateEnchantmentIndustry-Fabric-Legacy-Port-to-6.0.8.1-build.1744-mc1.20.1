package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PrinterDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof PrinterBlockEntity printer))
			return EMPTY_LINE;

		if (printer.getCopyTarget().isEmpty()) {
			// Replaced LANG.translate(...) with Component.translatable(...)
			return Component.translatable("gui.goggles.printer.no_target");
		} else if (printer.printEntry != null) {
			return printer.printEntry.getDisplaySourceContent(printer.getCopyTarget());
		} else {
			return EMPTY_LINE;
		}
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return false;
	}
}
