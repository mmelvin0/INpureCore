package info.inpureprojects.core;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import info.inpureprojects.core.API.Events.EventPreloaderRegister;
import info.inpureprojects.core.API.Events.INpureEventBus;
import info.inpureprojects.core.API.IINpureSubmodule;
import info.inpureprojects.core.API.IINpureSubmoduleExpanded;
import info.inpureprojects.core.API.IUpdateCheck;
import info.inpureprojects.core.API.PreloaderAPI;
import info.inpureprojects.core.API.Utils.LogWrapper;
import info.inpureprojects.core.Config.PropertiesHolder;
import info.inpureprojects.core.NEI.gtfoMicroblocks.Commands.CommandReload;
import info.inpureprojects.core.Preloader.ModuleManager;
import info.inpureprojects.core.Proxy.Proxy;
import info.inpureprojects.core.Updater.UpdateManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by den on 7/16/2014.
 */
@Mod(modid = modInfo.modid, name = modInfo.name, version = modInfo.version, dependencies = modInfo.deps)
public class INpureCore {

    @Mod.Instance(modInfo.modid)
    public static INpureCore instance;
    @SidedProxy(clientSide = modInfo.proxyClient, serverSide = modInfo.proxyCommon)
    public static Proxy proxy;
    public static PropertiesHolder properties;
    public static ArrayList<IINpureSubmodule> modules = new ArrayList();
    public static LogWrapper log;
    public static ArrayList<UpdateManager> managers = new ArrayList();
    public static File dir;

    public static void registerManager(IUpdateCheck check) {
        managers.add(new UpdateManager(check));
    }

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent evt) {
        PreloaderAPI.preLoaderEvents.register(this);
        log = new LogWrapper(evt.getModLog(), null);
        properties = new PropertiesHolder(new Configuration(new File(new File(evt.getModConfigurationDirectory(), "INpureProjects/INpureCore"), "INpureCore.cfg")));
        proxy.client();
        proxy.setupAPI();
        PreloaderAPI.preLoaderEvents.post(new EventPreloaderRegister());
        for (String s : ModuleManager.modules) {
            if (!properties.silence_submodule_logging) {
                proxy.print("Constructing submodule " + s);
            }
            try {
                IINpureSubmodule m = (IINpureSubmodule) Class.forName(s).newInstance();
                modules.add(m);
            } catch (Throwable t) {
                proxy.severe("Failed to load submodule " + s + "!");
                t.printStackTrace();
            }
        }
        dir = new File(evt.getModConfigurationDirectory(), "INpureProjects");
        for (IINpureSubmodule s : modules) {
            if (!properties.silence_submodule_logging) {
                proxy.print("Processing preinit event for submodule " + s.getClass().getName());
            }
            s.pre(dir);
        }
    }

    @INpureEventBus.INpureSubscribe
    public void registerModules(EventPreloaderRegister evt) {
        if (Loader.isModLoaded(modInfo.modid) && properties.updateCheck) {
            PreloaderAPI.modules.register("info.inpureprojects.core.Updater.UpdateModule");
        }
        if (properties.extract_scripts) {
            PreloaderAPI.modules.register("info.inpureprojects.core.Scripting.ScriptExtractor");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        for (IINpureSubmodule s : modules) {
            if (!properties.silence_submodule_logging) {
                proxy.print("Processing init event for submodule " + s.getClass().getName());
            }
            s.init();
        }
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent evt) {
        for (IINpureSubmodule s : modules) {
            if (!properties.silence_submodule_logging) {
                proxy.print("Processing postinit event for submodule " + s.getClass().getName());
            }
            s.post();
        }
        for (UpdateManager m : managers) {
            m.runCheck();
        }
    }

    @Mod.EventHandler
    public void onServer(FMLServerAboutToStartEvent evt) {
        proxy.onServerStartClient();
        for (IINpureSubmodule s : modules) {
            if (s instanceof IINpureSubmoduleExpanded) {
                if (!properties.silence_submodule_logging) {
                    proxy.print("Processing ServerAboutToStart event for submodule " + s.getClass().getName());
                }
                ((IINpureSubmoduleExpanded) s).onServerAboutToStart();
            }
        }
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new CommandReload());
    }

}
