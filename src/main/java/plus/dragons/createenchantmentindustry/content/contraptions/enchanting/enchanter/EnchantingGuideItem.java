package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

public class EnchantingGuideItem extends Item implements ExtendedScreenHandlerFactory {
	public EnchantingGuideItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		var level = pContext.getLevel();
		var player = pContext.getPlayer();
		if (player == null)
			return InteractionResult.PASS;

		if (player.isShiftKeyDown()) {
			var itemStack = pContext.getItemInHand();
			if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
				var blockPos = pContext.getClickedPos();
				var blockState = level.getBlockState(blockPos);

				if (blockState.getBlock() instanceof BlazeBurnerBlock) {
					if (!level.isClientSide()) {
						level.setBlockAndUpdate(blockPos, CeiBlocks.BLAZE_ENCHANTER.getDefaultState()
								.setValue(BlazeEnchanterBlock.FACING, blockState.getValue(BlazeBurnerBlock.FACING))
						);

						if (level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity tileEntity) {
							var i = itemStack.copy();
							i.setCount(1);
							tileEntity.setTargetItem(i);
						}

						AdvancementBehaviour.setPlacedBy(pContext.getLevel(), blockPos, player);
						CeiAdvancements.BLAZES_NEW_JOB.getTrigger().trigger((ServerPlayer) player);

						if (!player.getAbilities().instabuild)
							itemStack.shrink(1);
					}
					return InteractionResult.sidedSuccess(level.isClientSide());
				}
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
				serverPlayer.openMenu(this);
			}
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	@Override
	public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
		// CRITICAL FIX: Match the order expected by EnchantingGuideMenu(type, id, inv, buf)
		// 1. Boolean (directItemStackEdit)
		// 2. ItemStack (createOnClient reads this)
		buf.writeBoolean(true);
		buf.writeItem(player.getItemInHand(InteractionHand.MAIN_HAND));
	}

	@Override
	public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.current_enchantment").withStyle(s -> s.withColor(0x7F7F7F)));

		EnchantmentEntry enchantment = getEnchantment(pStack);
		if (enchantment == null) {
			pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.not_configured").withStyle(s -> s.withColor(0x7F7F7F)));
		} else {
			pTooltipComponents.add(enchantment.getFirst().getFullname(enchantment.getSecond()));
		}
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		return getEnchantment(pStack) != null;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		ItemStack heldItem = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
		return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem, null);
	}

	@Nullable
	public static EnchantmentEntry getEnchantment(ItemStack itemStack) {
		CompoundTag tag = itemStack.getTag();
		if (tag == null || !tag.contains("target", Tag.TAG_COMPOUND))
			return null;

		ItemStack book = ItemStack.of(tag.getCompound("target"));
		if (book.isEmpty()) return null;

		var enchantments = List.copyOf(EnchantmentHelper.getEnchantments(book).entrySet());
		if (enchantments.isEmpty())
			return null;

		int index = tag.getInt("index");
		if (index < 0 || index >= enchantments.size()) index = 0; // Fallback to first enchantment

		var result = enchantments.get(index);
		return EnchantmentEntry.of(result.getKey(), result.getValue());
	}
}
