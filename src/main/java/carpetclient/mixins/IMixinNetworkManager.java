package carpetclient.mixins;

import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(NetworkManager.class)
public interface IMixinNetworkManager {
    @Accessor
    Channel getChannel();
}
