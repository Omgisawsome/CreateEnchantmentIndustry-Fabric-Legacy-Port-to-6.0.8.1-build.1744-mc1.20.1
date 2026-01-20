package plus.dragons.createenchantmentindustry.foundation.advancement;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import net.minecraft.Util;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createdragonlib.advancement.AdvancementFactory;
import plus.dragons.createdragonlib.advancement.AdvancementHolder;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class CeiAdvancements {

	public static final AdvancementFactory ADVANCEMENT_FACTORY = AdvancementFactory.create(
			EnchantmentIndustry.MOD_ID,
			EnchantmentIndustry.MOD_ID,
			() -> {
				// Registration of triggers would go here if needed
			}
	);

	private static boolean registered = false;

	// Root
	public static final AdvancementHolder EXPERIENCED_ENGINEER = ADVANCEMENT_FACTORY.builder("experienced_engineer")
			.title(Component.translatable("advancement.cei.experienced_engineer"))
			.description(Component.translatable("advancement.cei.experienced_engineer.desc"))
			.icon(AllItems.EXP_NUGGET.get())
			.externalTrigger("have_experience_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(AllItems.EXP_NUGGET.get()))
			.parent(Create.asResource("display_board_0"))
			.build();

	// Printer Branch
	public static final AdvancementHolder BLACK_AS_INK = ADVANCEMENT_FACTORY.builder("black_as_ink")
			.title(Component.translatable("advancement.cei.black_as_ink"))
			.description(Component.translatable("advancement.cei.black_as_ink.desc"))
			.icon(CeiFluids.INK.get().getBucket())
			.externalTrigger("have_bucket_of_ink", InventoryChangeTrigger.TriggerInstance.hasItems(CeiFluids.INK.get().getBucket()))
			.parent(EXPERIENCED_ENGINEER)
			.build();

	public static final AdvancementHolder COPIABLE_MASTERPIECE = ADVANCEMENT_FACTORY.builder("copiable_masterpiece")
			.title(Component.translatable("advancement.cei.copiable_masterpiece"))
			.description(Component.translatable("advancement.cei.copiable_masterpiece.desc"))
			.icon(Items.WRITTEN_BOOK)
			.parent(BLACK_AS_INK)
			.build();

	public static final AdvancementHolder COPIABLE_MYSTERY = ADVANCEMENT_FACTORY.builder("copiable_mystery")
			.title(Component.translatable("advancement.cei.copiable_mystery"))
			.description(Component.translatable("advancement.cei.copiable_mystery.desc"))
			.icon(Items.ENCHANTED_BOOK)
			.announce(true)
			.parent(COPIABLE_MASTERPIECE)
			.build();

	public static final AdvancementHolder RELIC_RESTORATION = ADVANCEMENT_FACTORY.builder("relic_restoration")
			.title(Component.translatable("advancement.cei.relic_restoration"))
			.description(Component.translatable("advancement.cei.relic_restoration.desc"))
			.icon(Items.WRITABLE_BOOK)
			.announce(true)
			.frame(FrameType.GOAL)
			.parent(COPIABLE_MYSTERY)
			.build();

	public static final AdvancementHolder EMERGING_BRAND = ADVANCEMENT_FACTORY.builder("emerging_brand")
			.title(Component.translatable("advancement.cei.emerging_brand"))
			.description(Component.translatable("advancement.cei.emerging_brand.desc"))
			.icon(Items.NAME_TAG)
			.announce(true)
			.parent(COPIABLE_MASTERPIECE)
			.build();

	public static final AdvancementHolder GREAT_PUBLISHER = ADVANCEMENT_FACTORY.builder("great_publisher")
			.title(Component.translatable("advancement.cei.great_publisher"))
			.description(Component.translatable("advancement.cei.great_publisher.desc"))
			.icon(CeiBlocks.PRINTER.get())
			.announce(true)
			.frame(FrameType.CHALLENGE)
			.parent(RELIC_RESTORATION)
			.build();

	public static final AdvancementHolder EXPERIMENTAL = ADVANCEMENT_FACTORY.builder("experimental")
			.title(Component.translatable("advancement.cei.experimental"))
			.description(Component.translatable("advancement.cei.experimental.desc"))
			.icon(Items.EXPERIENCE_BOTTLE)
			.parent(EXPERIENCED_ENGINEER)
			.build();

	public static final AdvancementHolder GONE_WITH_THE_FOIL = ADVANCEMENT_FACTORY.builder("gone_with_the_foil")
			.title(Component.translatable("advancement.cei.gone_with_the_foil"))
			.description(Component.translatable("advancement.cei.gone_with_the_foil.desc"))
			.icon(CeiBlocks.DISENCHANTER.get())
			.parent(EXPERIMENTAL)
			.build();

	public static final AdvancementHolder SPIRIT_TAKING = ADVANCEMENT_FACTORY.builder("spirit_taking")
			.title(Component.translatable("advancement.cei.spirit_taking"))
			.description(Component.translatable("advancement.cei.spirit_taking.desc"))
			.icon(AllBlocks.MECHANICAL_PUMP.get())
			.announce(true)
			.parent(GONE_WITH_THE_FOIL)
			.build();

	public static final AdvancementHolder A_SHOWER_EXPERIENCE = ADVANCEMENT_FACTORY.builder("a_shower_experience")
			.title(Component.translatable("advancement.cei.a_shower_experience"))
			.description(Component.translatable("advancement.cei.a_shower_experience.desc"))
			.icon(AllBlocks.FLUID_PIPE.get())
			.announce(true)
			.frame(FrameType.GOAL)
			.parent(SPIRIT_TAKING)
			.build();

	public static final AdvancementHolder EXPERIENCED_RECYCLER = ADVANCEMENT_FACTORY.builder("experienced_recycler")
			.title(Component.translatable("advancement.cei.experienced_recycler"))
			.description(Component.translatable("advancement.cei.experienced_recycler.desc"))
			.icon(AllBlocks.COPPER_VALVE_HANDLE.get())
			.announce(true)
			.frame(FrameType.CHALLENGE)
			.parent(A_SHOWER_EXPERIENCE)
			.build();

	// Blaze Enchanter Branch
	public static final AdvancementHolder BLAZES_NEW_JOB = ADVANCEMENT_FACTORY.builder("blazes_new_job")
			.title(Component.translatable("advancement.cei.blazes_new_job"))
			.description(Component.translatable("advancement.cei.blazes_new_job.desc"))
			.icon(CeiItems.ENCHANTING_GUIDE.get())
			.parent(EXPERIENCED_ENGINEER)
			.build();

	public static final AdvancementHolder FIRST_ORDER = ADVANCEMENT_FACTORY.builder("first_order")
			.title(Component.translatable("advancement.cei.first_order"))
			.description(Component.translatable("advancement.cei.first_order.desc"))
			.icon(Items.GOLDEN_HELMET)
			.parent(BLAZES_NEW_JOB)
			.build();

	public static final AdvancementHolder ADDITIONAL_ORDER = ADVANCEMENT_FACTORY.builder("additional_order")
			.title(Component.translatable("advancement.cei.additional_order"))
			.description(Component.translatable("advancement.cei.additional_order.desc"))
			.icon(Util.make(
					new ItemStack(Items.GOLDEN_HELMET),
					stack -> stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 5)
			))
			.parent(FIRST_ORDER)
			.build();

	public static final AdvancementHolder HYPOTHETICAL_EXTENSION = ADVANCEMENT_FACTORY.builder("hypothetical_extension")
			.title(Component.translatable("advancement.cei.hypothetical_extension"))
			.description(Component.translatable("advancement.cei.hypothetical_extension.desc"))
			.icon(Items.DIAMOND_HELMET)
			.parent(ADDITIONAL_ORDER)
			.build();

	public static void register() {
		if (!registered) {
			ADVANCEMENT_FACTORY.register();
		}
		registered = true;
	}
}
