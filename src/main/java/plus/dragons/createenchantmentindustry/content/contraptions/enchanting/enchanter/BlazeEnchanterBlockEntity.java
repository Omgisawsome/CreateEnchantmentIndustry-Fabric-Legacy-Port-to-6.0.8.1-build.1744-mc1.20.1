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
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

public class BlazeEnchanterBlockEntity extends SmartBlockEntity
		implements MenuProvider, SidedStorageBlockEntity {

	public static final int ENCHANTING_TIME = 200;

	protected SmartFluidTankBehaviour internalTank;
	protected TransportedItemStack heldItem;
	public ItemStack targetItem = new ItemStack(CeiItems.ENCHANTING_GUIDE.get());
	protected int processingTicks;

	public boolean goggles;

	// Animation Fields
	public float headAngle;
	public float oHeadAngle;
	public float flip;
	public float oFlip;

	private static final Random bookRandom = new Random();
	protected final Random random = new Random();

	public final SnapshotParticipant<TransportedItemStack> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected TransportedItemStack createSnapshot() {
			return heldItem == null ? null : heldItem.copy();
		}

		@Override
		protected void readSnapshot(TransportedItemStack snapshot) {
			heldItem = snapshot;
		}

		@Override
		protected void onFinalCommit() {
			setChanged();
			notifyUpdate();
		}
	};

	protected final Map<Direction, LazyOptional<EnchantingItemHandler>> itemHandlers =
			new IdentityHashMap<>();

	public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			itemHandlers.put(d, LazyOptional.of(() -> new EnchantingItemHandler(this, d)));
		}
	}

	public ItemStack getHeldItemStack() {
		return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
	}

	public void setHeldItem(TransportedItemStack heldItem, Direction side) {
		this.heldItem = heldItem;
		this.heldItem.insertedFrom = side;
		setChanged();
		notifyUpdate();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this)
				.allowingBeltFunnels()
				.setInsertionHandler(this::tryInsertingFromSide));

		behaviours.add(internalTank = FilteringFluidTankBehaviour.single(
				fluid -> fluid.getFluid().is(CeiTags.FluidTag.BLAZE_ENCHANTER_INPUT.tag),
				this,
				(long) CeiConfigs.SERVER.blazeEnchanterTankCapacity.get() * UNIT_PER_MB
		));
	}

	@Override
	public void tick() {
		super.tick();

		if (level == null)
			return;

		if (level.isClientSide) {
			tickAnimation();
			return;
		}

		// Server Side Logic below
		if (heldItem == null) {
			processingTicks = 0;
			return;
		}

		if (processingTicks > 0) {
			processingTicks--;
			if (processingTicks == 0) continueProcessing();
			return;
		}

		heldItem.beltPosition += 0.125f;
		if (heldItem.beltPosition >= 0.5f) {
			var entry = Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper());
			if (entry != null) {
				processingTicks = ENCHANTING_TIME;
				setChanged();
				return;
			}
		}

		// Eject item if it reaches end of path
		if (heldItem.beltPosition >= 1.0f) {
			Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, heldItem.stack);
			heldItem = null;
			setChanged();
			notifyUpdate();
		}
	}

	private void tickAnimation() {
		// Store old values for smooth interpolation
		oHeadAngle = headAngle;
		oFlip = flip;

		// 1. Look At Player Logic
		Player player = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 10, false);

		if (player != null) {
			double dx = player.getX() - (worldPosition.getX() + 0.5);
			double dz = player.getZ() - (worldPosition.getZ() + 0.5);

			// Calculate angle in RADIANS
			double radians = Mth.atan2(dz, dx);

			// Convert to DEGREES
			// NEGATIVE converts the direction so Left goes Left (matches your previous success)
			float targetAngle = - (float) (radians * (180.0 / Math.PI));

			// Offset: -90 degrees flips it 180 compared to the previous +90
			// This should make it face YOU instead of away.
			targetAngle -= 90f;

			// Handle wrapping (degrees) so it doesn't spin 360 wildly
			float angleDiff = targetAngle - headAngle;
			while (angleDiff < -180f) angleDiff += 360f;
			while (angleDiff >= 180f) angleDiff -= 360f;

			// Smoothly rotate towards target
			headAngle += angleDiff * 0.1f;
		}

		// 2. Book Opening Logic
		boolean bookOpen = !targetItem.isEmpty() || (heldItem != null);

		if (bookOpen) {
			flip += 0.1f;
		} else {
			flip -= 0.1f;
		}
		flip = Mth.clamp(flip, 0, 1);
	}

	protected boolean continueProcessing() {
		boolean hyper = hyper();
		var entry = Enchanting.getValidEnchantment(heldItem.stack, targetItem, hyper);
		if (entry == null) return false;

		long amount = Enchanting.getExperienceConsumption(entry.getFirst(), entry.getSecond());
		Fluid requiredFluid = hyper ? CeiFluids.HYPER_EXPERIENCE : CeiFluids.EXPERIENCE;

		try (Transaction t = TransferUtil.getTransaction()) {
			long extracted = internalTank.getPrimaryHandler().extract(FluidVariant.of(requiredFluid), amount, t);

			if (extracted < amount) {
				t.abort();
				processingTicks = 0;
				return false;
			}

			Enchanting.Pair<Enchantment, Integer> pair = Enchanting.Pair.of(entry.getFirst(), entry.getSecond());
			Enchanting.enchantItem(heldItem.stack, pair);

			t.commit();
		}

		setChanged();
		return true;
	}

	protected ItemStack tryInsertingFromSide(TransportedItemStack stack, Direction side, boolean simulate) {
		if (heldItem != null) return stack.stack;

		// Reject insertion if we don't have ANY experience in the tank
		if (!hasAnyExperience()) return stack.stack;

		ItemStack inserted = stack.stack.copy();
		inserted.setCount(1);
		if (!simulate) {
			heldItem = stack.copy();
			heldItem.stack = inserted;
			heldItem.insertedFrom = side;
			setChanged();
		}
		return ItemHandlerHelper.copyStackWithSize(stack.stack, stack.stack.getCount() - 1);
	}

	public boolean hyper() {
		return internalTank.getPrimaryHandler().getFluid().getFluid().isSame(CeiFluids.HYPER_EXPERIENCE);
	}

	public boolean hasAnyExperience() {
		FluidStack fs = internalTank.getPrimaryHandler().getFluid();
		return !fs.isEmpty() && (fs.getFluid().isSame(CeiFluids.EXPERIENCE) || fs.getFluid().isSame(CeiFluids.HYPER_EXPERIENCE));
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level instanceof ServerLevel server) {
			if (heldItem != null)
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem.stack);
			var fluid = internalTank.getPrimaryHandler().getFluid();
			if (fluid.getFluid() instanceof ExperienceFluid)
				ExperienceFluid.drop(server, Vec3.atCenterOf(worldPosition), (int) (fluid.getAmount() / UNIT_PER_MB));
		}
	}

	public void setTargetItem(ItemStack targetItem) {
		this.targetItem = targetItem;
		setChanged();
		notifyUpdate();
	}

	@Override
	public void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putInt("Processing", processingTicks);
		tag.put("Target", NBTSerializer.serializeNBT(targetItem));
		tag.putBoolean("Goggles", goggles);
		if (heldItem != null) tag.put("Held", heldItem.serializeNBT());
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		processingTicks = tag.getInt("Processing");
		if (tag.contains("Target"))
			targetItem = ItemStack.of(tag.getCompound("Target"));
		goggles = tag.getBoolean("Goggles");
		heldItem = tag.contains("Held") ? TransportedItemStack.read(tag.getCompound("Held")) : null;
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return side != null && side.getAxis().isHorizontal() ? itemHandlers.get(side).getValueUnsafer() : null;
	}

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		return side == Direction.DOWN ? internalTank.getCapability() : null;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), id, inv, targetItem, worldPosition);
	}

	@Override
	public net.minecraft.network.chat.Component getDisplayName() {
		return targetItem.getHoverName();
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, targetItem);
	}
}
