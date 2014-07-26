package info.inpureprojects.core;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import info.inpureprojects.core.Config.PropertiesHolder;
import info.inpureprojects.core.Events.INpureHandler;
import info.inpureprojects.core.Minecraft.ForgeHandler;
import info.inpureprojects.core.Minecraft.MinecraftHandler;
import info.inpureprojects.core.Preloader.INpurePreLoader;
import info.inpureprojects.core.Proxy.Proxy;
import info.inpureprojects.core.Scripting.ScriptingCore;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by den on 7/16/2014.
 */
@Mod(modid = modInfo.modid, name = modInfo.name, version = modInfo.version)
public class INpureCore {

    @Mod.Instance
    public static INpureCore instance;
    @SidedProxy(clientSide = modInfo.proxyClient, serverSide = modInfo.proxyCommon)
    public static Proxy proxy;
    public static ScriptingCore core;
    public static File configFolder;
    public static INpureHandler scriptHandler;
    public static File libsFolder;
    public static Configuration config;
    public static PropertiesHolder properties;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent evt) {
        instance = this;
        libsFolder = new File(evt.getSourceFile().getParent(), Loader.MC_VERSION);
        libsFolder.mkdirs();
        configFolder = new File(evt.getModConfigurationDirectory(), "INpureCore");
        config = new Configuration(new File(configFolder, "INpureCore.cfg"));
        properties = new PropertiesHolder(config);
        scriptHandler = new INpureHandler(configFolder);
        core = new ScriptingCore();
        core.bus.register(scriptHandler);
        core.bus.register(new MinecraftHandler());
        MinecraftForge.EVENT_BUS.register(new ForgeHandler());
        core.doSetup();
        if (INpurePreLoader.isDev) {
            proxy.devStuff();
        }
        proxy.extractCore();
        core.loadScripts();
        core.doLoad();
        proxy.client();
        core.forwardingBus.post(evt);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        proxy.collectData();
        core.forwardingBus.post(evt);
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent evt) {
        core.forwardingBus.post(evt);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {

    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent evt) {
        core.doSave();
    }

}
