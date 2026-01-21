package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HyperExperienceBottleItem extends Item {

	public HyperExperienceBottleItem(Properties pProperties) {
		super(pProperties);
	}

	/**
	 * Makes the bottle always have the enchantment glint/glow.
	 */
	@Override
	public boolean isFoil(ItemStack pStack) {
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);

		// Play the throw sound
		pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
				SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL,
				0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));

		if (!pLevel.isClientSide) {
			// Instantiate the custom entity
			HyperExperienceBottle bottle = new HyperExperienceBottle(pPlayer, pLevel);
			bottle.setItem(itemstack);

			// Set trajectory: -20.0F is the pitch offset to make it arc correctly
			bottle.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), -20.0F, 0.7F, 1.0F);

			pLevel.addFreshEntity(bottle);
		}

		pPlayer.awardStat(Stats.ITEM_USED.get(this));

		// Consume the item if not in creative
		if (!pPlayer.getAbilities().instabuild) {
			itemstack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
	}
}
