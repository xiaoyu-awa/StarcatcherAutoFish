package me.xiaoyu.autofish;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;


public final class StarCatcherAutoFishAccessor {
    private static final String SCREEN_FQCN = "com.wdiscute.starcatcher.minigame.FishingMinigameScreen";
    private static final String SPOT_FQCN = "com.wdiscute.starcatcher.minigame.ActiveSweetSpot";
    private static final String NIKDO_FQCN = "com.wdiscute.starcatcher.registry.minigamemodifiers.Nikdo53Modifier";
    private static final String BOBBER_FQCN = "com.wdiscute.starcatcher.bobberentity.FishingBobEntity";
    private static final String ROD_FQCN = "com.wdiscute.starcatcher.registry.items.rod.StarcatcherFishingRodItem";

    private static volatile boolean initialized = false;
    private static volatile boolean available = false;
    private static volatile boolean nikdoAvailable = false;
    private static volatile boolean bobberAvailable = false;
    private static volatile boolean rodAvailable = false;

    private static Class<?> screenClass;
    private static Class<?> spotClass;
    private static Class<?> nikdoClass;
    private static Class<?> bobberClass;
    private static Class<?> rodItemClass;

    private static Method getActiveSweetSpots;
    private static Method getModifiers;
    private static Method getPointerPosPrecise;
    private static Method inputPressed;
    private static Method doDegreesOverlapWithLeeway;
    private static Method getSpotLayer;

    private static Field spotPos;
    private static Field spotThickness;
    private static Field pointerLayer;
    private static Field bobberStateField;

    private StarCatcherAutoFishAccessor() {
    }

    public static boolean isAvailable() {
        if (!initialized) init();
        return available;
    }

    private static synchronized void init() {
        if (initialized) return;
        try {
            screenClass = Class.forName(SCREEN_FQCN);
            spotClass = Class.forName(SPOT_FQCN);

            getActiveSweetSpots = screenClass.getMethod("getActiveSweetSpots");
            getModifiers = screenClass.getMethod("getModifiers");
            getPointerPosPrecise = screenClass.getMethod("getPointerPosPrecise");
            inputPressed = screenClass.getMethod("inputPressed");
            doDegreesOverlapWithLeeway = screenClass.getMethod("doDegreesOverlapWithLeeway", float.class, float.class, int.class);

            spotPos = spotClass.getField("pos");
            spotThickness = spotClass.getField("thickness");

            available = true;
        } catch (Throwable t) {
            available = false;
        }

        try {
            nikdoClass = Class.forName(NIKDO_FQCN);
            pointerLayer = nikdoClass.getField("pointerLayer");
            getSpotLayer = nikdoClass.getDeclaredMethod("getSpotLayer", spotClass);
            getSpotLayer.setAccessible(true);
            nikdoAvailable = true;
        } catch (Throwable t) {
            nikdoAvailable = false;
        }

        try {
            bobberClass = Class.forName(BOBBER_FQCN);
            bobberStateField = bobberClass.getField("STATE");
            bobberAvailable = true;
        } catch (Throwable t) {
            bobberAvailable = false;
        }

        try {
            rodItemClass = Class.forName(ROD_FQCN);
            rodAvailable = true;
        } catch (Throwable t) {
            rodAvailable = false;
        }
        initialized = true;
        StarCatcherAutoFish.LOGGER.info("Accessor init: minigame={}, nikdo={}, bobber={}, rod={}",
                available, nikdoAvailable, bobberAvailable, rodAvailable);
    }

    public static boolean isMinigameScreen(Screen screen) {
        return screen != null && isAvailable() && screenClass.isInstance(screen);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getActiveSweetSpots(Object screen) {
        try {
            return (List<Object>) getActiveSweetSpots.invoke(screen);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getModifiers(Object screen) {
        try {
            return (List<Object>) getModifiers.invoke(screen);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    public static Object findNikdoModifier(Object screen) {
        if (!isAvailable() || !nikdoAvailable) return null;
        for (Object mod : getModifiers(screen)) {
            if (nikdoClass.isInstance(mod)) return mod;
        }
        return null;
    }

    public static int getPointerLayer(Object modifier) {
        try {
            return pointerLayer.getInt(modifier);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void setPointerLayer(Object modifier, int layer) {
        try {
            pointerLayer.setInt(modifier, layer);
        } catch (Throwable t) {
        }
    }

    public static int getSpotLayer(Object spot) {
        try {
            return (int) getSpotLayer.invoke(null, spot);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static float getPointerPosPrecise(Object screen) {
        try {
            return (float) getPointerPosPrecise.invoke(screen);
        } catch (Throwable t) {
            return Float.NaN;
        }
    }

    public static void callInputPressed(Object screen) {
        try {
            inputPressed.invoke(screen);
        } catch (Throwable t) {
        }
    }

    public static boolean doDegreesOverlapWithLeeway(float deg1, float deg2, int leeway) {
        try {
            return (boolean) doDegreesOverlapWithLeeway.invoke(null, deg1, deg2, leeway);
        } catch (Throwable t) {
            return false;
        }
    }

    public static float getSpotPos(Object spot) {
        try {
            return (float) spotPos.get(spot);
        } catch (Throwable t) {
            return Float.NaN;
        }
    }

    public static int getSpotThickness(Object spot) {
        try {
            return spotThickness.getInt(spot);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static boolean isBobberAvailable() {
        if (!initialized) init();
        return bobberAvailable;
    }

    public static boolean isRodAvailable() {
        if (!initialized) init();
        return rodAvailable;
    }

    public static boolean isBobber(Entity entity) {
        return bobberAvailable && entity != null && bobberClass.isInstance(entity);
    }

    @SuppressWarnings("unchecked")
    public static int getBobberState(Entity bobber) {
        try {
            EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) bobberStateField.get(null);
            return bobber.getEntityData().get(accessor);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static boolean isRodItem(ItemStack stack) {
        if (!rodAvailable || stack == null) return false;
        try {
            return rodItemClass.isInstance(stack.getItem());
        } catch (Throwable t) {
            return false;
        }
    }
}
