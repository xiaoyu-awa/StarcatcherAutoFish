package me.xiaoyu.autofish;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(StarCatcherAutoFish.MODID)
public class StarCatcherAutoFish {
    public static final String MODID = "starcatcherautofish";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StarCatcherAutoFish() {
        LOGGER.info("StarCatcherAutoFish loading");
    }
}
