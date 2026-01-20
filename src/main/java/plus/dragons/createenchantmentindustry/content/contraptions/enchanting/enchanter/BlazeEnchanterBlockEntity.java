package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class BlazeEnchanterBlockEntity extends SmartBlockEntity
		implements MenuProvider, SidedStorageBlockEntity {

	public static final int ENCHANTING_TIME = 200;

	protected SmartFluidTankBehaviour internalTank;
	protected TransportedItemStack heldItem;
	protected ItemStack targetItem = new ItemStack(CeiItems.ENCHANTING_GUIDE.get());
	protected int processingTicks;

	public boolean goggles;

	protected final Map<Direction, LazyOptional<EnchantingItemHandler>> itemHandlers =
			new IdentityHashMap<>();

	protected final Random random = new Random();

	public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			itemHandlers.put(d, LazyOptional.of(() -> new EnchantingItemHandler(this, d)));
		}
	}

	public ItemStack getHeldItemStack() {
		return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this)
				.allowingBeltFunnels()
				.setInsertionHandler(this::tryInsertingFromSide));

		behaviours.add(internalTank = FilteringFluidTankBehaviour.single(
				fluid -> fluid.getFluid().is(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag),
				this,
				CeiConfigs.SERVER.blazeEnchanterTankCapacity.get()
		));
	}

	@Override
	public void tick() {
		super.tick();

		if (level == null || level.isClientSide)
			return;

		if (heldItem == null) {
			processingTicks = 0;
			return;
		}

		if (processingTicks > 0) {
			processingTicks--;
			if (processingTicks == 0) {
				continueProcessing();
			}
			return;
		}

		heldItem.beltPosition += 0.125f;

		if (heldItem.beltPosition >= 0.5f) {
			var entry = Enchanting.getValidEnchantment(
					heldItem.stack, targetItem, hyper());

			if (entry != null) {
				processingTicks = ENCHANTING_TIME;
				setChanged();
			}
		}
	}

	protected boolean continueProcessing() {
		var entry = Enchanting.getValidEnchantment(
				heldItem.stack, targetItem, hyper());

		if (entry == null)
			return false;

		// FIX: Use the specific Pair type defined in the Enchanting class
		Enchanting.Pair<Enchantment, Integer> pair = Enchanting.Pair.of(entry.getFirst(), entry.getSecond());
		Enchanting.enchantItem(heldItem.stack, pair);

		FluidStack cost = new FluidStack(
				hyper()
						? CeiFluids.HYPER_EXPERIENCE.get().getSource()
						: CeiFluids.EXPERIENCE.get().getSource(),
				(long) Enchanting.getExperienceConsumption(
						entry.getFirst(), entry.getSecond())
		);

		try (Transaction t = TransferUtil.getTransaction()) {
			internalTank.getPrimaryHandler().extract(
					cost.getType(), cost.getAmount(), t);
			t.commit();
		}

		heldItem = null;
		setChanged();
		return true;
	}

	protected ItemStack tryInsertingFromSide(
			TransportedItemStack stack, Direction side, boolean simulate) {

		if (heldItem != null)
			return stack.stack;

		ItemStack inserted = stack.stack.copy();
		inserted.setCount(1);

		if (!simulate) {
			heldItem = stack.copy();
			heldItem.stack = inserted;
			heldItem.insertedFrom = side;
			setChanged();
		}

		return ItemHandlerHelper.copyStackWithSize(
				stack.stack, stack.stack.getCount() - 1);
	}

	public boolean hyper() {
		return CeiFluids.HYPER_EXPERIENCE.is(
				internalTank.getPrimaryHandler().getFluid().getFluid());
	}

	@Override
	public void destroy() {
		super.destroy();

		if (level instanceof ServerLevel server) {
			if (heldItem != null)
				Containers.dropItemStack(level,
						worldPosition.getX(),
						worldPosition.getY(),
						worldPosition.getZ(),
						heldItem.stack);

			var fluid = internalTank.getPrimaryHandler().getFluid();
			if (fluid.getFluid() instanceof ExperienceFluid exp)
				exp.drop(server,
						Vec3.atCenterOf(worldPosition),
						(int) fluid.getAmount());
		}
	}

	@Override
	public void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putInt("Processing", processingTicks);
		tag.put("Target", NBTSerializer.serializeNBT(targetItem));
		tag.putBoolean("Goggles", goggles);
		if (heldItem != null)
			tag.put("Held", heldItem.serializeNBT());
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		processingTicks = tag.getInt("Processing");
		targetItem = ItemStack.of(tag.getCompound("Target"));
		goggles = tag.getBoolean("Goggles");
		heldItem = tag.contains("Held")
				? TransportedItemStack.read(tag.getCompound("Held"))
				: null;
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return side != null && side.getAxis().isHorizontal()
				? itemHandlers.get(side).getValueUnsafer()
				: null;
	}

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		return side == Direction.DOWN ? internalTank.getCapability() : null;
	}

	@Override
	public AbstractContainerMenu createMenu(
			int id, Inventory inv, Player player) {

		return new EnchantingGuideMenu(
				CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(),
				id, inv, targetItem, worldPosition);
	}

	@Override
	public net.minecraft.network.chat.Component getDisplayName() {
		return targetItem.getHoverName();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		return new ItemRequirement(
				ItemRequirement.ItemUseType.CONSUME, targetItem);
	}
}
