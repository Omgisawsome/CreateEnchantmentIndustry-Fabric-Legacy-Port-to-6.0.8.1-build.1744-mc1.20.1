package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlazeEnchanterEditPacket extends SimplePacketBase {

	private int index;
	private ItemStack itemStack;
	private BlockPos blockPos;

	public BlazeEnchanterEditPacket(int index, ItemStack itemStack, BlockPos blockPos) {
		this.index = index;
		this.itemStack = itemStack;
		this.blockPos = blockPos;
	}

	public BlazeEnchanterEditPacket(FriendlyByteBuf buffer) {
		this.index = buffer.readInt();
		this.itemStack = buffer.readItem();
		this.blockPos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(index);
		buffer.writeItem(itemStack);
		buffer.writeBlockPos(blockPos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) return;
			Level level = player.level();

			if (level.isLoaded(blockPos) && level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity be) {
				// CRITICAL FIX: The itemStack from the client usually has the old NBT.
				// We MUST update the "index" tag to match the selection made in the GUI.
				ItemStack result = itemStack.copy();
				CompoundTag tag = result.getOrCreateTag();
				tag.putInt("index", index);

				// Now set the updated item on the block entity
				be.setTargetItem(result);
			}
		});
		return true;
	}
}
