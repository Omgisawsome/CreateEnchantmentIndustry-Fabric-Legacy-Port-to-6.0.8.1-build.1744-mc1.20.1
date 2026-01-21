package plus.dragons.createenchantmentindustry.foundation.mixin.fabric;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public interface OpenEndFluidHandlerAccessor {

	/**
	 * Grabs the parent OpenEndedPipe instance from the inner handler class.
	 */
	@Accessor("this$0")
	OpenEndedPipe getPipe();

}
