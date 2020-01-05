package carpetclient.coders.zhuyifei1999;

import carpetclient.mixins.IMixinNetworkManager;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.util.ObfuscationUtilities;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;

// Forge has net.minecraftforge.fml.common.network.handshake.NetworkDispatcher,
// which registers itself in the pipeline with name "fml:packet_handler" before
// the vanilla. It eats up all the packets regarding channel registration, so
// the vanilla handler never sees those packets. Unfortunately, LiteLoader's
// packet handler hooks on the vanilla handler, so it would never get those
// needed packets.
//
// As a workaround, we register our handler before Forge and if we detect Forge's
// between us and vanilla, and the packet concerns plugin channel registration,
// we send the packet directly to LiteLoader before forge eats it.
//
// TODO: Perhaps upstream this to LiteLoader?

public class ForgePluginChannelRegister {
    public static final String HANDLER_NAME = "carpetmod:packet_handler";

    public static void handle(NetworkManager networkManager) {
        if (!ObfuscationUtilities.fmlIsPresent())
            return;

        Channel channel = ((IMixinNetworkManager) networkManager).getChannel();
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addBefore("packet_handler", HANDLER_NAME, new SimpleChannelInboundHandler<Packet<?>>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
                List<String> handlerNames = pipeline.names();
                int thisInd = handlerNames.indexOf(HANDLER_NAME),
                    fmlInd = handlerNames.indexOf("fml:packet_handler"),
                    mcInd = handlerNames.indexOf("packet_handler");

                if (
                    thisInd >= 0 && fmlInd > 0 && mcInd > 0 &&
                    thisInd < fmlInd && fmlInd < mcInd
                ) {
                    if (msg instanceof CPacketCustomPayload) {
                        CPacketCustomPayload packet = (CPacketCustomPayload) msg;
                        String channelName = packet.getChannelName();

                        if ("REGISTER".equals(channelName) || "UNREGISTER".equals(channelName)) {
                            LiteLoader.getServerPluginChannels().onPluginChannelMessage(networkManager.getNetHandler(), packet);
                        }
                    } else if (msg instanceof SPacketCustomPayload) {
                        SPacketCustomPayload packet = (SPacketCustomPayload) msg;
                        String channelName = packet.getChannelName();

                        if ("REGISTER".equals(channelName) || "UNREGISTER".equals(channelName)) {
                            LiteLoader.getClientPluginChannels().onPluginChannelMessage(packet);
                        }
                    }
                }

                ctx.fireChannelRead(msg);
            }
        });
    }
}
