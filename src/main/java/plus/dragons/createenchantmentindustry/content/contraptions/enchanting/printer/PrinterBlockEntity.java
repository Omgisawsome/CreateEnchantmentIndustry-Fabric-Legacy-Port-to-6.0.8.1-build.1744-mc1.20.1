package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.List;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.FilteringFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiTriggers;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;
import plus.dragons.createenchantmentindustry.entry.CeiTags;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import org.jetbrains.annotations.Nullable;

public class PrinterBlockEntity extends SmartBlockEntity implements SidedStorageBlockEntity {

	public static final int COPYING_TIME = 100;
	protected BeltProcessingBehaviour beltProcessing;
	public FilteringFluidTankBehaviour tank;
	public int processingTicks;

	public final SimpleContainer copyTargetInventory = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			notifyUpdate();
		}
	};
	public final InventoryStorage copyTargetStorage = InventoryStorage.of(copyTargetInventory, null);

	public boolean tooExpensive;

	public PrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		processingTicks = -1;
		tooExpensive = false;
	}

	public ItemStack getCopyTarget() {
		return copyTargetInventory.getItem(0);
	}

	public void setCopyTarget(ItemStack stack) {
		copyTargetInventory.setItem(0, stack);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		// FIXED: Added the filter argument (argument 1)
		// This ensures it calls FilteringFluidTankBehaviour.single(...) instead of SmartFluidTankBehaviour.single(...)
		tank = FilteringFluidTankBehaviour
				.single(
						// Filter: Allow fluids that are valid Printer inputs (Ink, XP, etc.)
						fluid -> CeiTags.FluidTag.PRINTER_INPUT.matches(fluid.getFluid()),
						this,
						(int) (CeiConfigs.SERVER.printerTankCapacity.get() * EnchantmentIndustry.UNIT_PER_MB) // Cast to int
				);

		tank.allowExtraction();
		tank.allowInsertion();

		behaviours.add(tank);

		behaviours.add(beltProcessing = new BeltProcessingBehaviour(this)
				.whenItemEnters(this::onItemReceived)
				.whileItemHeld(this::whenItemHeld));
	}

	protected BeltProcessingBehaviour.ProcessingResult onItemReceived(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		if (handler.blockEntity.isVirtual()) return PASS;

		ItemStack copyTarget = getCopyTarget();
		if (tooExpensive || copyTarget.isEmpty()) return PASS;
		if (!Printing.isValid(transported.stack)) return PASS;
		if (tank.getPrimaryHandler().getResource().isBlank()) return HOLD;
		if (Printing.isTooExpensive(copyTarget, CeiConfigs.SERVER.printerTankCapacity.get())) return PASS;

		return HOLD;
	}

	protected BeltProcessingBehaviour.ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		if (processingTicks != -1 && processingTicks != 5) return HOLD;

		ItemStack copyTarget = getCopyTarget();
		if (tooExpensive || copyTarget.isEmpty()) return PASS;
		if (!Printing.isValid(transported.stack)) return PASS;

		if (processingTicks == -1) {
			processingTicks = COPYING_TIME;
			notifyUpdate();
			return HOLD;
		}

		long requiredAmount = Printing.getRequiredAmountForItem(copyTarget);
		if (requiredAmount == -1) return PASS;

		FluidVariant requiredFluid = Printing.getRequiredFluidForItem(copyTarget);
		if (requiredFluid == null) return PASS;

		try (Transaction t = Transaction.openOuter()) {
			long extractable = tank.getPrimaryHandler().extract(requiredFluid, requiredAmount, t);
			if (extractable < requiredAmount) return HOLD;
		}

		ItemStack result = Printing.process(copyTarget, transported.stack, tank.getPrimaryHandler());
		if (result != null) {
			List<TransportedItemStack> outList = new java.util.ArrayList<>();
			TransportedItemStack resultStack = transported.copy();
			resultStack.stack = result;
			outList.add(resultStack);
			handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, transported));

			triggerAdvancement();

			notifyUpdate();
			return PASS;
		}

		return HOLD;
	}

	protected void triggerAdvancement() {
		if (level != null && !level.isClientSide) {
			Player player = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
			if (player instanceof ServerPlayer serverPlayer) {
				CeiTriggers.BOOK_PRINTED.trigger(serverPlayer, 1);
			}
		}
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");
		tooExpensive = compound.getBoolean("tooExpensive");
		if (compound.contains("copyTargetInventory"))
			copyTargetInventory.fromTag(compound.getList("copyTargetInventory", 10));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("ProcessingTicks", processingTicks);
		compound.putBoolean("tooExpensive", tooExpensive);
		compound.put("copyTargetInventory", copyTargetInventory.createTag());
	}

	@Override
	public void tick() {
		super.tick();
		if (processingTicks >= 0) processingTicks--;
		if (processingTicks >= 50 && level.isClientSide) spawnParticles();
	}

	protected void spawnParticles() {
		if (copyTargetInventory.isEmpty() || tank.getPrimaryHandler().getResource().isBlank()) return;
		Vec3 center = Vec3.atCenterOf(worldPosition);
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return copyTargetStorage;
	}
}
