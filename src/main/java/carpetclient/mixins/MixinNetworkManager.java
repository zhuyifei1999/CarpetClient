package carpetclient.mixins;

import carpetclient.coders.zhuyifei1999.ForgePluginChannelRegister;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelActive", at = @At("RETURN"))
    private void forgeChannelRegisterRemote(ChannelHandlerContext ctx, CallbackInfo ci) {
        ForgePluginChannelRegister.handle((NetworkManager)(Object)this);
    }
}
