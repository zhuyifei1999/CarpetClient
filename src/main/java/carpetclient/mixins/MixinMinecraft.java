package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.mixinInterface.AMixinTimer;
import carpetclient.rules.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Tick rate editing in Minecraft.java based on Cubitecks tick rate mod.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {
    @Shadow
    private @Final
    Timer timer;
    @Shadow
    private boolean isGamePaused;
    @Shadow
    public WorldClient world;
    @Shadow
    public EntityPlayerSP player;

    /**
     * Reset logic for clipping through pistons.
     *
     * @param ci
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    public void fixingPistons(CallbackInfo ci) {
        PistonFix.resetBools();
    }

    /**
     * Tick the player at the rate of player rate
     *
     * @param ci
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    public void tickPlayer(CallbackInfo ci) {
        if (
            this.world != null && this.player != null && !this.isGamePaused &&
            ((AMixinTimer) this.timer).getElapsedTicksPlayer() > 0 &&
            !this.player.isDead
        ) {
            try {
                this.world.updateEntity(this.player);
            } catch (Throwable e) {
                CrashReport cr = CrashReport.makeCrashReport(e, "Ticking player");
                CrashReportCategory crcat = cr.makeCategory("Player being ticked");
                this.player.addEntityCrashInfo(crcat);
                throw new ReportedException(cr);
            }
        }
    }

    /**
     * Modify constant in scroll mouse to fix the issue when slowing down.
     */
    @ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
    private long runTickMouseFix(long value) {
        if (TickRate.runTickRate) {
            return (long) Math.max(200F * (20.0f / Config.tickRate), 200L);
        } else {
            return 200L;
        }
    }
}
