package plus.dragons.createenchantmentindustry.foundation.advancement;

import net.minecraft.advancements.CriteriaTriggers;
import plus.dragons.createdragonlib.advancement.critereon.AccumulativeTrigger;
import plus.dragons.createdragonlib.advancement.critereon.SimpleTrigger;
import plus.dragons.createdragonlib.advancement.critereon.TriggerFactory;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import static plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements.ADVANCEMENT_FACTORY;

public class CeiTriggers {
	private static final TriggerFactory FACTORY = ADVANCEMENT_FACTORY.getTriggerFactory();

	// Accumulative triggers for counting progress
	public static final AccumulativeTrigger BOOK_PRINTED = FACTORY.accumulative(EnchantmentIndustry.genRL("book_printed"));
	public static final AccumulativeTrigger DISENCHANTED = FACTORY.accumulative(EnchantmentIndustry.genRL("disenchanted"));

	// Simple trigger for one-time events like absorbing player XP
	public static final SimpleTrigger SPIRIT_TAKING = FACTORY.simple(EnchantmentIndustry.genRL("spirit_taking"));

	/**
	 * Call this method in your mod's onInitialize() to register triggers.
	 * This ensures Minecraft recognizes the criteria in your advancement JSONs.
	 */
	public static void register() {
		CriteriaTriggers.register(BOOK_PRINTED);
		CriteriaTriggers.register(DISENCHANTED);
		CriteriaTriggers.register(SPIRIT_TAKING);
	}
}
