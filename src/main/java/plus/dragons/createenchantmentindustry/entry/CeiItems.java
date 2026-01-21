package plus.dragons.createenchantmentindustry.entry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.Rarity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

public class CeiItems {

	// FIXED: Removed the static block that was causing ClassCastException

	public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
			.properties(prop -> prop.stacksTo(1))
			.tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()) // Set tab here instead
			.register();

	public static final ItemEntry<HyperExperienceBottleItem> HYPER_EXP_BOTTLE = REGISTRATE.item("hyper_experience_bottle", HyperExperienceBottleItem::new)
			.properties(prop -> prop.rarity(Rarity.RARE))
			.lang("Bottle O' Hyper Enchanting")
			.tag(CeiTags.ItemTag.UPRIGHT_ON_BELT.tag)
			.tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()) // Set tab here instead
			.register();

	public static void register() {}
}
