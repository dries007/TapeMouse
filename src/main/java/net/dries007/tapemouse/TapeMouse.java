package net.dries007.tapemouse;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

/**
 * Main mod file
 * @author Dries007
 */
@Mod("tapemouse")
public class TapeMouse
{
    public static final Logger LOGGER = LogManager.getLogger();

    public TapeMouse()
    {
        // Why oh why. Why can this not be a thing in the toml or @Mod like it used to be...
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        // Register client event handler only if we're on the client.
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEventHandler::new);
    }
}
