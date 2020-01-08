package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.mixins.IMixinMinecraft;
import carpetclient.mixinInterface.AMixinMinecraft;
import carpetclient.mixinInterface.AMixinTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Shadow
    private @Final Minecraft mc;

    @Shadow
    public abstract void renderWorld(float partialTicks, long finishTimeNano);

    /**
     * fixes the world being culled while noclipping
     */
    @Redirect(method = "renderWorldPass(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    private boolean fixSpectator(EntityPlayerSP player) {
        return player.isSpectator() || (Config.creativeModeNoClip.getValue() && player.isCreative());
    }

    /**
     * fix tick rate rendering glitch rendering player
     */
    @Redirect(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V"))
    private void tickratePlayerPartial(EntityRenderer thisarg, float partialTicksWorld, long finishTimeNano) {
        // Normally, entities are rendered at partial ticks in between last tick
        // pos and current pos. However, players are affected here at player tick
        // rate and the world's partial ticks does not reflect that of the player,
        // causing very wrong velocities and "rubberbanding" every world tick.
        //
        // Because mods often hook rendering to create overlays (for example
        // bounding boxes or schematics), demanding the computation of entity
        // render position to switch between world and player partial ticks is
        // impractical for every mod out there. We do instead by computing where
        // the last tick pos is supposed to be to show the player at the correct
        // position and velocity. I.e. The following vector equations are satisfied:
        //
        //   worldlastpos  + (worldcurpos  - worldlastpos ) * worldpartialticks
        // = playerlastpos + (playercurpos - playerlastpos) * playerpartialticks
        //
        //   (worldcurpos  - worldlastpos ) / worldtickrate
        // = (playercurpos - playerlastpos) / playertickrate
        //
        // where player{last,cur}pos is what is given in the entity data and
        // world{last,cur}pos is what we are computing.

        double savedLastX = this.mc.player.lastTickPosX;
        double savedLastY = this.mc.player.lastTickPosY;
        double savedLastZ = this.mc.player.lastTickPosZ;
        double savedCurX = this.mc.player.posX;
        double savedCurY = this.mc.player.posY;
        double savedCurZ = this.mc.player.posZ;

        Timer timer = ((IMixinMinecraft) this.mc).getTimer();
        float partialTicksPlayer = this.mc.isGamePaused() ?
            ((AMixinMinecraft) this.mc).getRenderPartialTicksPausedPlayer() :
            ((AMixinTimer) timer).getRenderPartialTicksPlayer();
        float rateMultiplier = ((AMixinTimer) timer).getWorldTickRate() /
            ((AMixinTimer) timer).getPlayerTickRate();

        // I wish preprocessor macros are a thing :( I could use Tuple but they do references
        {
            double diffraw = this.mc.player.posX - this.mc.player.lastTickPosX;
            double sum = this.mc.player.lastTickPosX + diffraw * partialTicksPlayer;
            double diff = diffraw * rateMultiplier;
            this.mc.player.lastTickPosX = sum - diff * partialTicksWorld;
            this.mc.player.posX = diff + this.mc.player.lastTickPosX;
        }
        {
            double diffraw = this.mc.player.posY - this.mc.player.lastTickPosY;
            double sum = this.mc.player.lastTickPosY + diffraw * partialTicksPlayer;
            double diff = diffraw * rateMultiplier;
            this.mc.player.lastTickPosY = sum - diff * partialTicksWorld;
            this.mc.player.posY = diff + this.mc.player.lastTickPosY;
        }
        {
            double diffraw = this.mc.player.posZ - this.mc.player.lastTickPosZ;
            double sum = this.mc.player.lastTickPosZ + diffraw * partialTicksPlayer;
            double diff = diffraw * rateMultiplier;
            this.mc.player.lastTickPosZ = sum - diff * partialTicksWorld;
            this.mc.player.posZ = diff + this.mc.player.lastTickPosZ;
        }

        try {
            this.renderWorld(partialTicksWorld, finishTimeNano);
        } finally {
            this.mc.player.lastTickPosX = savedLastX;
            this.mc.player.lastTickPosY = savedLastY;
            this.mc.player.lastTickPosZ = savedLastZ;
            this.mc.player.posX = savedCurX;
            this.mc.player.posY = savedCurY;
            this.mc.player.posZ = savedCurZ;
        }
    }
}
