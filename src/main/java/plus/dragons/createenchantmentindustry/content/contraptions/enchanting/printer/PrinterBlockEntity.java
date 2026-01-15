package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;
import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.ExperienceFluid;
import plus.dragons.createenchantmentindustry.entry.CeiTags;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

public class PrinterBlockEntity extends SmartBlockEntity implements SidedStorageBlockEntity {

	public static final int COPYING_TIME = 100;
	protected BeltProcessingBehaviour beltProcessing;
	public int processingTicks;
	SmartFluidTankBehaviour tank;
	private ItemStack copyTarget;
	public boolean tooExpensive;
	public PrintEntry printEntry;
	boolean sendParticles;
	LazyOptional<PrinterTargetItemHandler> itemHandler = LazyOptional.of(() -> new PrinterTargetItemHandler(this));

	SnapshotParticipant<ItemStack> snapshotParticipant = new SnapshotParticipant<>() {
		@Override
		protected ItemStack createSnapshot() {
			return getCopyTarget();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			setCopyTarget(snapshot.isEmpty() ? null : snapshot);
		}

		@Override
		protected void onFinalCommit() {
			notifyUpdate();
		}
	};

	public PrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		processingTicks = -1;
		copyTarget = null;
		tooExpensive = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(tank = FilteringFluidTankBehaviour
				.single(fluidStack -> fluidStack.getFluid().is(CeiTags.FluidTag.PRINTER_INPUT.tag),
						this, CeiConfigs.SERVER.copierTankCapacity.get() * UNIT_PER_MB));
		behaviours.add(beltProcessing = new BeltProcessingBehaviour(this)
				.whenItemEnters(this::onItemReceived)
				.whileItemHeld(this::whenItemHeld));
	}

	public void tick() {
		super.tick();
		if (processingTicks >= 0)
			processingTicks--;
	}

	public ItemStack getCopyTarget() {
		return copyTarget == null ? ItemStack.EMPTY : copyTarget;
	}

	public void setCopyTarget(@NotNull ItemStack copyTarget) {
		if (copyTarget.isEmpty()) {
			this.copyTarget = null;
			tooExpensive = false;
			printEntry = null;
		} else {
			this.copyTarget = copyTarget;
			matchPrintEntry(copyTarget);
			tooExpensive = Printing.isTooExpensive(printEntry, copyTarget,
					CeiConfigs.SERVER.copierTankCapacity.get() * UNIT_PER_MB);
		}
		processingTicks = -1;
		notifyUpdate();
	}

	private void matchPrintEntry(ItemStack copyTarget) {
		var entry = Printing.match(copyTarget);
		if (entry == null) {
			this.copyTarget = null;
			tooExpensive = false;
		}
		printEntry = entry;
	}

	protected static int ENCHANT_PARTICLE_COUNT = 20;

	protected void spawnParticles() {
		if (level.isClientSide())
			return;
		Vec3 center = Vec3.atCenterOf(worldPosition).subtract(0, 11 / 16f, 0);
		ParticleOptions particle = ParticleTypes.ENCHANT;
		for (int i = 0; i < ENCHANT_PARTICLE_COUNT; i++) {
			Vec3 m = new Vec3(level.random.nextDouble(), Math.abs(level.random.nextDouble()), level.random.nextDouble());
			level.addAlwaysVisibleParticle(particle, center.x, center.y, center.z, m.x, m.y, m.z);
		}
		level.playLocalSound(center.x, center.y, center.z, SoundEvents.ENCHANTMENT_TABLE_USE,
				SoundSource.BLOCKS, 1f, level.random.nextFloat() * 0.1f + 0.9f, true);
	}

	protected BeltProcessingBehaviour.ProcessingResult onItemReceived(TransportedItemStack transported,
																	  TransportedItemStackHandlerBehaviour handler) {
		if (handler.blockEntity.isVirtual())
			return PASS;
		if (tooExpensive || copyTarget == null)
			return PASS;
		if (!Printing.valid(printEntry, copyTarget, transported.stack))
			return PASS;
		if (tank.isEmpty() || Printing.isCorrectInk(printEntry, getCurrentFluidInTank(), copyTarget))
			return HOLD;
		if (Printing.getRequiredAmountForItem(printEntry, copyTarget) == -1)
			return PASS;
		return HOLD;
	}

	protected BeltProcessingBehaviour.ProcessingResult whenItemHeld(TransportedItemStack transported,
																	TransportedItemStackHandlerBehaviour handler) {
		if (processingTicks != -1 && processingTicks != 10)
			return HOLD;
		if (tooExpensive || copyTarget == null)
			return PASS;
		if (!Printing.valid(printEntry, copyTarget, transported.stack))
			return PASS;
		if (tank.isEmpty() || !Printing.isCorrectInk(printEntry, getCurrentFluidInTank(), copyTarget))
			return HOLD;

		FluidStack fluid = getCurrentFluidInTank();
		int requiredAmount = Printing.getRequiredAmountForItem(printEntry, copyTarget);
		if (requiredAmount == -1)
			return PASS;
		if (requiredAmount > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = COPYING_TIME;
			notifyUpdate();
			return HOLD;
		}

		// Process finished
		ItemStack copy = Printing.print(printEntry, copyTarget, requiredAmount, transported.stack, fluid);
		transported.stack = copy;
		tank.getPrimaryHandler().setFluid(fluid);
		sendParticles = true;
		notifyUpdate();
		return HOLD;
	}

	private FluidStack getCurrentFluidInTank() {
		return tank.getPrimaryHandler().getFluid();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (level instanceof ServerLevel serverLevel) {
			if (copyTarget != null)
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), copyTarget);
			var fluidStack = tank.getPrimaryHandler().getFluid();
			if (fluidStack.getFluid() instanceof ExperienceFluid expFluid)
				expFluid.drop(serverLevel, Vec3.atCenterOf(worldPosition), (int) fluidStack.getAmount());
		}
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putInt("ProcessingTicks", processingTicks);
		tag.putBoolean("tooExpensive", tooExpensive);
		if (copyTarget != null)
			tag.put("copyTarget", NBTSerializer.serializeNBT(copyTarget));
		if (sendParticles && clientPacket) {
			tag.putBoolean("SpawnParticles", true);
			sendParticles = false;
		}
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		copyTarget = null;
		processingTicks = tag.getInt("ProcessingTicks");
		tooExpensive = tag.getBoolean("tooExpensive");
		if (tag.contains("copyTarget")) {
			copyTarget = ItemStack.of(tag.getCompound("copyTarget"));
			matchPrintEntry(copyTarget);
		}
		if (!clientPacket)
			return;
		if (tag.contains("SpawnParticles"))
			spawnParticles();
	}

	@Override
	public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		if (side != Direction.DOWN)
			return tank.getCapability();
		return null;
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return itemHandler.getValueUnsafer();
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().expandTowards(0, -2, 0);
	}

	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(Component.translatable("gui.goggles.printer"));
		if (copyTarget == null) {
			tooltip.add(Component.translatable("gui.goggles.printer.no_target").withStyle(ChatFormatting.GRAY));
		} else {
			printEntry.addToGoggleTooltip(tooltip, isPlayerSneaking, copyTarget);
		}
		return true;
	}
}
