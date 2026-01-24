package plus.dragons.createenchantmentindustry.entry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiBlockPartials {

	public static final PartialModel
			// Blaze Enchanter Models
			BLAZE_ENCHANTER_IDLE = block("blaze_enchanter/blaze_idle"),
			BLAZE_ENCHANTER_ACTIVE = block("blaze_enchanter/blaze_active"),
			BLAZE_ENCHANTER_IDLE_HYPER = block("blaze_enchanter/blaze_idle_hyper"),
			BLAZE_ENCHANTER_ACTIVE_HYPER = block("blaze_enchanter/blaze_active_hyper"),

	// Printer Models
	PRINTER_TOP = block("printer/top"),
			PRINTER_MIDDLE = block("printer/middle"),
			PRINTER_BOTTOM = block("printer/bottom");

	private static PartialModel block(String path) {
		// FABRIC/FLYWHEEL FIX: Use the static .of() factory method instead of 'new'
		return PartialModel.of(EnchantmentIndustry.genRL("block/" + path));
	}

	public static void register() {
		// Flywheel usually handles the discovery of static PartialModels automatically
		// if the class is loaded, but this method exists for entry point consistency.
	}

}
