package me.xiaoyu.autofish;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = StarCatcherAutoFish.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StarCatcherAutoFishClient {
    public static boolean enabled = true;
    public static boolean autoCastEnabled = false;

    private static int useHoldTicks = 0;
    private static int useCooldownTicks = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ClientModBusEvents.TOGGLE_KEY.consumeClick()) {
            enabled = !enabled;
            StarCatcherAutoFish.LOGGER.info("AutoFish {}", enabled ? "on" : "off");
            mc.player.displayClientMessage(
                    Component.translatable(enabled ? "starcatcherautofish.action.on" : "starcatcherautofish.action.off"),
                    true
            );
        }

        while (ClientModBusEvents.AUTO_CAST_KEY.consumeClick()) {
            autoCastEnabled = !autoCastEnabled;
            StarCatcherAutoFish.LOGGER.info("AutoCast {}", autoCastEnabled ? "on" : "off");
            mc.player.displayClientMessage(
                    Component.translatable(autoCastEnabled ? "starcatcherautofish.autocast.on" : "starcatcherautofish.autocast.off"),
                    true
            );
            if (!autoCastEnabled && useHoldTicks > 0) {
                useHoldTicks = 0;
                mc.options.keyUse.setDown(false);
            }
        }

        if (autoCastEnabled) {
            handleAutoCast(mc);
        }

        if (!enabled) return;
        if (!StarCatcherAutoFishAccessor.isAvailable()) return;

        Screen screen = mc.screen;
        if (!StarCatcherAutoFishAccessor.isMinigameScreen(screen)) return;

        Object minigame = screen;
        float pointerPos = StarCatcherAutoFishAccessor.getPointerPosPrecise(minigame);
        if (Float.isNaN(pointerPos)) return;

        Object nikdo = StarCatcherAutoFishAccessor.findNikdoModifier(minigame);

        List<Object> spots = StarCatcherAutoFishAccessor.getActiveSweetSpots(minigame);
        for (Object spot : spots) {
            float spotPos = StarCatcherAutoFishAccessor.getSpotPos(spot);
            int thickness = StarCatcherAutoFishAccessor.getSpotThickness(spot);
            int halfThickness = thickness / 2;

            if (StarCatcherAutoFishAccessor.doDegreesOverlapWithLeeway(pointerPos, spotPos, halfThickness)) {
                if (nikdo != null) {
                    int spotLayer = StarCatcherAutoFishAccessor.getSpotLayer(spot);
                    if (spotLayer != StarCatcherAutoFishAccessor.getPointerLayer(nikdo)) {
                        StarCatcherAutoFishAccessor.setPointerLayer(nikdo, spotLayer);
                    }
                }
                StarCatcherAutoFishAccessor.callInputPressed(minigame);
                return;
            }
        }
    }

    private static void handleAutoCast(Minecraft mc) {
        if (!StarCatcherAutoFishAccessor.isBobberAvailable()) return;

        if (useCooldownTicks > 0) useCooldownTicks--;

        if (mc.screen != null) {
            if (useHoldTicks > 0) {
                useHoldTicks = 0;
                mc.options.keyUse.setDown(false);
            }
            return;
        }

        if (useHoldTicks > 0) {
            useHoldTicks--;
            mc.options.keyUse.setDown(true);
            if (useHoldTicks == 0) {
                mc.options.keyUse.setDown(false);
                useCooldownTicks = 15;
            }
            return;
        }

        if (useCooldownTicks > 0) return;

        if (StarCatcherAutoFishAccessor.isRodAvailable()) {
            boolean hasRod = StarCatcherAutoFishAccessor.isRodItem(mc.player.getMainHandItem())
                    || StarCatcherAutoFishAccessor.isRodItem(mc.player.getOffhandItem());
            if (!hasRod) return;
        }

        Entity bobber = findMyBobber(mc);
        if (bobber == null) {
            StarCatcherAutoFish.LOGGER.info("Auto cast");
            useHoldTicks = 2;
        } else if (StarCatcherAutoFishAccessor.getBobberState(bobber) == 3) {
            StarCatcherAutoFish.LOGGER.info("Auto reel");
            useHoldTicks = 2;
        }
    }

    private static Entity findMyBobber(Minecraft mc) {
        if (mc.level == null) return null;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!StarCatcherAutoFishAccessor.isBobber(entity)) continue;
            if (entity instanceof Projectile proj && proj.getOwner() == mc.player) {
                return entity;
            }
        }
        return null;
    }
}
