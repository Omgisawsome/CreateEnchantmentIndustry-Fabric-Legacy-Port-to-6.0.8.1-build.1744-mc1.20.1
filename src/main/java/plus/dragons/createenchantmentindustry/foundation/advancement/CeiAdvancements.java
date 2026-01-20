package plus.dragons.createenchantmentindustry.foundation.advancement;

import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Items;
import plus.dragons.createdragonlib.advancement.AdvancementFactory;
import plus.dragons.createdragonlib.advancement.AdvancementHolder;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiAdvancements {
	// FIXED: Changed "Enchantment Industry" to EnchantmentIndustry.MOD_ID
	// This prevents the ResourceLocationException (no spaces allowed!)
	public static final AdvancementFactory ADVANCEMENT_FACTORY = AdvancementFactory.create(
			EnchantmentIndustry.MOD_ID,
			EnchantmentIndustry.MOD_ID,
			CeiAdvancements::register
	);

	public static final AdvancementHolder START = ADVANCEMENT_FACTORY.builder("root")
			.title("Create: Enchantment Industry")
			.description("The industry of enchanting must grow!")
			.icon(Items.ENCHANTING_TABLE)
			.externalTrigger("have_book", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.BOOK).build()))
			.build();

	public static final AdvancementHolder EXPERIENCED_ENGINEER = ADVANCEMENT_FACTORY.builder("experienced_engineer")
			.title("Experienced Engineer")
			.description("Install a Disenchanter or a Printer")
			.icon(Items.BOOK)
			.parent(START)
			.externalTrigger("have_disenchanter", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.BOOK).build()))
			.build();

	public static final AdvancementHolder BLACK_AS_INK = ADVANCEMENT_FACTORY.builder("black_as_ink")
			.title("Black as Ink!")
			.description("Get a bucket of Ink for your publishing business")
			.icon(Items.BUCKET)
			.parent(EXPERIENCED_ENGINEER)
			.externalTrigger("have_ink", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BUCKET))
			.build();

	public static final AdvancementHolder A_SHOWER_EXPERIENCE = ADVANCEMENT_FACTORY.builder("a_shower_experience")
			.title("A Shower of Experience")
			.description("Stand under the liquid experience from the Disenchanter")
			.icon(Items.EXPERIENCE_BOTTLE)
			.parent(EXPERIENCED_ENGINEER)
			.externalTrigger("shower_experience", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EXPERIENCE_BOTTLE))
			.build();

	public static final AdvancementHolder EXPERIENCED_RECYCLER = ADVANCEMENT_FACTORY.builder("experienced_recycler")
			.title("Experienced Recycler")
			.description("Recycle 1,000,000 mB of experience from Disenchanter")
			.icon(Items.EXPERIENCE_BOTTLE)
			.announce(true)
			.frame(FrameType.CHALLENGE)
			.parent(A_SHOWER_EXPERIENCE)
			.externalTrigger("recycle_experience", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EXPERIENCE_BOTTLE))
			.build();

	public static final AdvancementHolder GREAT_PUBLISHER = ADVANCEMENT_FACTORY.builder("great_publisher")
			.title("The Great Publisher")
			.description("Print 1,000 items with the Printer")
			.icon(Items.WRITABLE_BOOK)
			.announce(true)
			.frame(FrameType.CHALLENGE)
			.parent(BLACK_AS_INK)
			.externalTrigger("print_items", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WRITABLE_BOOK))
			.build();

	public static final AdvancementHolder EXPERIMENTAL = ADVANCEMENT_FACTORY.builder("experimental")
			.title("Experimental")
			.description("Perform a disenchantment")
			.icon(Items.GLASS_BOTTLE)
			.parent(EXPERIENCED_ENGINEER)
			.externalTrigger("experimental", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLASS_BOTTLE))
			.build();

	public static final AdvancementHolder GONE_WITH_THE_FOIL = ADVANCEMENT_FACTORY.builder("gone_with_the_foil")
			.title("Gone with the Foil")
			.description("Disenchant an enchanted item")
			.icon(Items.BOOK)
			.parent(EXPERIMENTAL)
			.externalTrigger("gone_with_the_foil", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BOOK))
			.build();

	public static final AdvancementHolder SPIRIT_TAKING = ADVANCEMENT_FACTORY.builder("spirit_taking")
			.title("Spirit Taking")
			.description("Collect experience from a Mob")
			.icon(Items.BONE)
			.parent(A_SHOWER_EXPERIENCE)
			.externalTrigger("spirit_taking", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
			.build();

	public static final AdvancementHolder BLAZES_NEW_JOB = ADVANCEMENT_FACTORY.builder("blazes_new_job")
			.title("Blaze's New Job")
			.description("Give an Enchanting Guide to a Blaze Burner")
			.icon(Items.BLAZE_POWDER)
			.parent(START)
			.externalTrigger("blazes_new_job", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BLAZE_POWDER))
			.build();

	public static void register() {
	}

	public static void fill() {
	}
}
