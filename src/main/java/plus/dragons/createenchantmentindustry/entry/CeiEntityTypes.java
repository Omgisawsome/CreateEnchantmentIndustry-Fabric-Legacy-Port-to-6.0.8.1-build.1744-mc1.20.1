package plus.dragons.createenchantmentindustry.entry;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottle;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrb;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrbRenderer;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

public class CeiEntityTypes {

	public static final EntityEntry<HyperExperienceOrb> HYPER_EXPERIENCE_ORB = REGISTRATE
			.<HyperExperienceOrb>entity("hyper_experience_orb", (type, level) -> new HyperExperienceOrb(type, level), MobCategory.MISC)
			.renderer(() -> HyperExperienceOrbRenderer::new)
			// FABRIC FIX: Use .dimensions() instead of .sized(), and trackingRange() / forceTracked()
			.properties(b -> b.dimensions(EntityDimensions.fixed(0.5f, 0.5f))
					.trackRangeBlocks(6)
					.trackedUpdateRate(20))
			.register();

	public static final EntityEntry<HyperExperienceBottle> HYPER_EXPERIENCE_BOTTLE = REGISTRATE
			.<HyperExperienceBottle>entity("hyper_experience_bottle", (type, level) -> new HyperExperienceBottle(type, level), MobCategory.MISC)
			.renderer(() -> ThrownItemRenderer::new)
			// FABRIC FIX: Use .dimensions() instead of .sized()
			.properties(b -> b.dimensions(EntityDimensions.fixed(0.25f, 0.25f))
					.trackRangeBlocks(4)
					.trackedUpdateRate(10))
			.lang("Thrown Bottle O' Hyper Enchanting")
			.register();

	public static void register() {
	}
}
