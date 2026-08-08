package me.xiaoyu.autofish;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StarCatcherAutoFish.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEvents {
    public static final String CATEGORY = "key.categories.starcatcherautofish";

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.starcatcherautofish.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_HOME,
            CATEGORY
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }
}
