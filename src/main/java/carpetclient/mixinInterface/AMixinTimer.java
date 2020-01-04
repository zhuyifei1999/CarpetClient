package carpetclient.mixinInterface;

/**
 * Duck interface for MixinTimer.java
 */
public interface AMixinTimer {
    int getElapsedTicksPlayer();

    void setWorldTickRate(float tps);
}
