package carpetclient.random;

import carpetclient.Config;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;

public class RandomtickDisplay {
    private static final String gold = "\u00a76", red = "\u00a74", green = "\u00a72", pushe = "\u00a76Pushes", pull = "\u00a76Pull";
    private static ArrayList<BlockPos> chunks = new ArrayList<>();

    public static void processPacket(PacketBuffer data) {
        NBTTagCompound nbt;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nbt != null && Minecraft.getMinecraft().world != null) {
            addDisplay(nbt);
        }
    }

    private static void addDisplay(NBTTagCompound nbt) {
        if (!Config.randomtickChunkUpdates) return;

        NBTTagList nbttaglist = nbt.getTagList("list", 10);

        chunks.clear();
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound chunkData = nbttaglist.getCompoundTagAt(i);
            int x = chunkData.getInteger("x");
            int z = chunkData.getInteger("z");
            chunks.add(new BlockPos(x * 16 + 8, 0, z * 16 + 8));
        }
    }

    public static void draw(float partialTicks) {
        if (!Config.randomtickingChunksVisualizer.getValue()) return;

        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        final double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        final double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        final double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        final RenderManager rm = Minecraft.getMinecraft().getRenderManager();

        int counter = 0;
        for (BlockPos pos : chunks) {
            EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, gold + counter, (float) (pos.getX() + 0.5f - d0), (float) (entity.posY + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
            counter++;
        }
    }

    public static void startStopRecording(boolean start) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RANDOMTICK_DISPLAY);
        sender.writeBoolean(start);
        CarpetPluginChannel.packatSender(sender);
    }

    public static void reset() {
        Config.randomtickingChunksVisualizer.setValue(false);
    }
}
