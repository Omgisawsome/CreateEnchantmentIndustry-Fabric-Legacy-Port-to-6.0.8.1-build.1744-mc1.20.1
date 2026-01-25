package plus.dragons.createenchantmentindustry.foundation.config;

import net.createmod.catnip.config.ConfigBase;

import net.createmod.catnip.config.ui.ConfigAnnotations;
import net.minecraftforge.common.ForgeConfigSpec;


public class CeiServerConfig extends ConfigBase {
	// Tank Capacities
	public final ConfigInt disenchanterTankCapacity = i(1000, 0, "disenchanterTankCapacity", "The Tank Capacity of the Disenchanter");
	public final ConfigInt copierTankCapacity = i(4000, 0, "copierTankCapacity", "The Tank Capacity of the Copier");
	public final ConfigInt blazeEnchanterTankCapacity = i(2000, 0, "blazeEnchanterTankCapacity", "The Tank Capacity of the Blaze Enchanter");
	public final ConfigInt printerTankCapacity = i(1000, 0, "printerTankCapacity", "The Tank Capacity of the Printer");

	// Mechanics
	public final ConfigInt maxHyperEnchantingLevelExtension = i(2, 0, "maxHyperEnchantingLevelExtension", "Max level extension for hyper-enchanting");
	public final ConfigFloat deployerXpDropChance = f(1, 0, 1, "deployerXpDropChance", "Chance for Deployer-killed entities to drop XP nuggets");
	public final ConfigBool enableHyperEnchant = b(true, "enableHyperEnchant");

	// Costs
	public final ConfigFloat enchantByBlazeEnchanterCostCoefficient = f(1, 0.01f, 100, "enchantByBlazeEnchanterCostCoefficient");
	public final ConfigFloat hyperEnchantByBlazeEnchanterCostCoefficient = f(1, 0.01f, 100, "hyperEnchantByBlazeEnchanterCostCoefficient");
	public final ConfigFloat copyEnchantedBookCostCoefficient = f(1, 0.01f, 100, "copyEnchantedBookCostCoefficient");
	public final ConfigFloat copyEnchantedBookWithHyperExperienceCostCoefficient = f(1, 0.01f, 100, "copyEnchantedBookWithHyperExperienceCostCoefficient");

	public final ConfigInt copyWrittenBookCostPerPage = i(5, 1, 100, "copyWrittenBookCostPerPage", "Ink cost per page");
	public final ConfigInt copyNameTagCost = i(7, 1, 1000, "copyNameTagCost", "XP cost for Name Tags");
	public final ConfigInt copyTrainScheduleCost = i(10, 1, 1000, "copyTrainScheduleCost", "Ink cost for Train Schedules");
	public final ConfigInt copyClipboardCost = i(10, 1, 1000, "copyClipboardCost", "Ink cost for Clipboards");

	// World Interaction
	public final ConfigFloat crushingWheelDropExpRate = f(0.3f, 0, 1, "crushingWheelDropExpRate", "Chance of XP nuggets from Crushing Wheel");
	public final ConfigFloat crushingWheelDropExpScale = f(0.34F, 0.1F, 100, "crushingWheelDropExpScale", "Scale of XP nuggets from Crushing Wheel");
	public final ConfigBool copyingWrittenBookAlwaysGetOriginalVersion = b(true, "copyingWrittenBookAlwaysGetOriginalVersion", "If true, copying written books produces originals");

	@Override
	public String getName() {
		return "server";
	}
}
