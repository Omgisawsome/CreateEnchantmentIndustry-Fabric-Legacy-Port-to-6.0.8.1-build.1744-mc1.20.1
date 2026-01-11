package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.ComparatorUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import plus.dragons.createenchantmentindustry.entry.CeiBlockEntities;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class DisenchanterBlock extends Block
		implements IWrenchable, IBE<DisenchanterBlockEntity> {

	public DisenchanterBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Class<DisenchanterBlockEntity> getBlockEntityClass() {
		return DisenchanterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends DisenchanterBlockEntity> getBlockEntityType() {
		return CeiBlockEntities.DISENCHANTER.get();
	}

	@Override
	public InteractionResult use(
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hit
	) {
		if (hand == InteractionHand.OFF_HAND)
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);

		if (heldItem.isEmpty()) {
			return onBlockEntityUse(level, pos, be -> {
				if (!be.getHeldItemStack().isEmpty()) {
					if (!level.isClientSide) {
						player.setItemInHand(hand, be.heldItem.stack);
						be.heldItem = null;
						be.notifyUpdate();
					}
					return InteractionResult.sidedSuccess(level.isClientSide);
				}
				return InteractionResult.PASS;
			});
		}

		if (hit.getDirection() == Direction.UP) {
			return onBlockEntityUse(level, pos, be -> {
				if (be.getHeldItemStack().isEmpty()) {
					ItemStack insert = heldItem.copy();
					insert.setCount(1);

					var result = Disenchanting.disenchantResult(insert, level);
					if (result != null) {
						if (!level.isClientSide) {
							be.heldItem = new TransportedItemStack(insert);
							be.notifyUpdate();
							heldItem.shrink(1);
						}
					}
					return InteractionResult.sidedSuccess(level.isClientSide);
				}
				return InteractionResult.PASS;
			});
		}

		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context
	) {
		return AllShapes.CASING_13PX.get(Direction.UP);
	}

	@Override
	public void onRemove(
			BlockState state,
			Level level,
			BlockPos pos,
			BlockState newState,
			boolean isMoving
	) {
		IBE.onRemove(state, level, pos, newState);
	}

	@Override
	public void setPlacedBy(
			Level level,
			BlockPos pos,
			BlockState state,
			@Nullable LivingEntity placer,
			ItemStack stack
	) {
		super.setPlacedBy(level, pos, state, placer, stack);
		AdvancementBehaviour.setPlacedBy(level, pos, placer);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> drops = new ArrayList<>();
		drops.add(CeiBlocks.DISENCHANTER.asStack());
		return drops;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return ComparatorUtil.levelOfSmartFluidTank(level, pos);
	}

	@Override
	public boolean isPathfindable(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			PathComputationType type
	) {
		return false;
	}
}
