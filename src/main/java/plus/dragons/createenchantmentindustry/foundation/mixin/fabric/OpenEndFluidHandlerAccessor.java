package plus.dragons.createenchantmentindustry.foundation.mixin.fabric;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public interface OpenEndFluidHandlerAccessor {

	@Accessor(value = "this$0", remap = false)
	OpenEndedPipe getPipe();
}
