package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public class HyperExperienceBottleItem extends Item {

	public HyperExperienceBottleItem(Properties pProperties) {
		super(pProperties);
	}

	/**
	 * Gives the bottle the enchantment glint.
	 */
	@Override
	public boolean isFoil(ItemStack pStack) {
		return true;
	}

	/**
	 * Create's Smart Pipes and other systems can use this to identify
	 * the fluid associated with this bottle for filtering purposes.
	 */
	public FluidVariant getFluid(ItemStack stack) {
		return FluidVariant.of(CeiFluids.HYPER_EXPERIENCE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);

		pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
				SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL,
				0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));

		if (!pLevel.isClientSide) {
			HyperExperienceBottle bottle = new HyperExperienceBottle(pPlayer, pLevel);
			bottle.setItem(itemstack);
			// Standard projectile logic
			bottle.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), -20.0F, 0.7F, 1.0F);
			pLevel.addFreshEntity(bottle);
		}

		pPlayer.awardStat(Stats.ITEM_USED.get(this));

		if (!pPlayer.getAbilities().instabuild) {
			itemstack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
	}
}
