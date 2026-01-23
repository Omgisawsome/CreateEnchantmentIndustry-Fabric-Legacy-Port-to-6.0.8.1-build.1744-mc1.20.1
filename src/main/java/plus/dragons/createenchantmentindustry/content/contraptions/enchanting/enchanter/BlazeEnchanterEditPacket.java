package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlazeEnchanterEditPacket extends SimplePacketBase {
	private final int index;
	private final ItemStack itemStack;
	private final BlockPos pos;

	public BlazeEnchanterEditPacket(int index, ItemStack itemStack, BlockPos pos) {
		this.index = index;
		this.itemStack = itemStack;
		this.pos = pos;
	}

	public BlazeEnchanterEditPacket(FriendlyByteBuf buffer) {
		this.index = buffer.readInt();
		this.itemStack = buffer.readItem();
		this.pos = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(index);
		buffer.writeItem(itemStack);
		buffer.writeBlockPos(pos);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null || sender.level() == null) return;

			BlockEntity be = sender.level().getBlockEntity(pos);
			if (be instanceof BlazeEnchanterBlockEntity enchanter) {
				// FIX: Access targetItem directly instead of getGuide()
				ItemStack guide = enchanter.targetItem;

				if (guide != null && !guide.isEmpty()) {
					CompoundTag tag = guide.getOrCreateTag();
					tag.putInt("index", index);

					if (itemStack.isEmpty()) {
						tag.remove("target");
					} else {
						// Use the standard saving method for 1.20.1
						tag.put("target", itemStack.save(new CompoundTag()));
					}

					enchanter.setChanged();
					enchanter.sendData(); // This syncs the change to clients
				}
			}
		});
		return true;
	}
}
