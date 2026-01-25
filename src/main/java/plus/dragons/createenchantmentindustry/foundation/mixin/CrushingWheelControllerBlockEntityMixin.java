package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.createmod.catnip.math.VecHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

@Mixin(CrushingWheelControllerBlockEntity.class)
public class CrushingWheelControllerBlockEntityMixin {
	@Shadow(remap = false)
	public Entity processingEntity;

	@Inject(method = "tick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V", shift = At.Shift.AFTER))
	private void injected(CallbackInfo ci) {
		if (processingEntity instanceof LivingEntity livingEntity && !processingEntity.isAlive()) {
			int reward = Math.max(
					(int) Math.floor(livingEntity.getExperienceReward() * CeiConfigs.SERVER.crushingWheelDropExpScale.get()),
					1
			);

			if (reward >= 1000 || Math.random() < CeiConfigs.SERVER.crushingWheelDropExpRate.get()) {
				int count = reward / 3 + (Math.random() < (reward % 3 / 3f) ? 1 : 0);
				if (count != 0) {
					var self = (CrushingWheelControllerBlockEntity) (Object) this;

					// Replacement for VecHelper.getCenterOf
					Vec3 centerPos = new Vec3(
							self.getBlockPos().getX() + 0.5,
							self.getBlockPos().getY() + 0.5,
							self.getBlockPos().getZ() + 0.5
					);

					Direction facing = self.getBlockState().getValue(CrushingWheelControllerBlock.FACING);
					int offset = facing.getAxisDirection().getStep();

					Vec3 outSpeed = new Vec3(
							(facing.getAxis() == Direction.Axis.X ? 0.25D : 0.0D) * offset,
							offset == 1 ? (facing.getAxis() == Direction.Axis.Y ? 0.5D : 0.0D) : 0.0D,
							(facing.getAxis() == Direction.Axis.Z ? 0.25D : 0.0D) * offset
					);

					Vec3 outPos = centerPos.add(
							(facing.getAxis() == Direction.Axis.X ? 0.55f * offset : 0f),
							(facing.getAxis() == Direction.Axis.Y ? 0.55f * offset : 0f),
							(facing.getAxis() == Direction.Axis.Z ? 0.55f * offset : 0f)
					);

					var expItem = new ItemEntity(
							processingEntity.level(),
							outPos.x(),
							outPos.y(),
							outPos.z(),
							new ItemStack(AllItems.EXP_NUGGET.get(), count)
					);
					expItem.setDeltaMovement(outSpeed);
					expItem.getCustomData().put("BypassCrushingWheel", NbtUtils.writeBlockPos(self.getBlockPos()));
					processingEntity.level().addFreshEntity(expItem);
				}
			}
		}
	}
}
