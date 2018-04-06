package carpetclient;

import java.io.File;
import java.util.List;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.PostRenderListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.PluginChannelListener;
import carpetclient.pluginchannel.CarpetPluginChannel;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

public class LiteModCarpetClient implements Tickable, LiteMod, PluginChannelListener, PostRenderListener {

    private boolean gameRunnin = false;

    @Override
    public String getVersion() {
        return "@VERSION@";
    }

    @Override
    public void init(File configPath) {
        Hotkeys.init();
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public String getName() {
        return "Carpet Client";
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        gameRunnin = minecraft.isIntegratedServerRunning() || minecraft.getCurrentServerData() != null;

        if (gameRunnin) {
            Hotkeys.onTick(minecraft, partialTicks, inGame, clock);
        }
    }

    // Needed method for plugin channels. Data from the server.
    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        CarpetPluginChannel.packatReceiver(channel, data);
    }

    // Needed method for plugin channels. Adds the list of channels that the client will listen for.
    @Override
    public List<String> getChannels() {
        return CarpetPluginChannel.CARPET_PLUGIN_CHANNEL;
    }

    @Override
    public void onPostRenderEntities(float partialTicks) {
        if (gameRunnin) {
            MainRender.mainRender(partialTicks);
        }
    }

    @Override
    public void onPostRender(float partialTicks) {

    }
}