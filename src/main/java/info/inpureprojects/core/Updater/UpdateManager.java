package info.inpureprojects.core.Updater;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import info.inpureprojects.core.API.IUpdateCheck;
import info.inpureprojects.core.API.ReleaseLevel;
import info.inpureprojects.core.INpureCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Created by den on 9/18/2014.
 */
public class UpdateManager {

    private UThread thread;
    private int lastPoll = 400;
    private boolean alreadyDisplayed = false;

    public UpdateManager(IUpdateCheck check) {
        this.thread = new UThread(check);
        FMLCommonHandler.instance().bus().register(this);
    }

    public void runCheck() {
        thread.start();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) {
            return;
        }
        if (lastPoll > 0) {
            --lastPoll;
            return;
        }
        lastPoll = 400;
        if (!this.thread.update.getLevel().equals(ReleaseLevel.PUBLIC)) {
            FMLCommonHandler.instance().bus().unregister(this);
            return;
        }
        if (!alreadyDisplayed && thread.checkComplete) {
            if (thread.updateAvailable) {
                EntityPlayer player = evt.player;
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[" + thread.update.getModName() + "]:").appendText(EnumChatFormatting.WHITE + " A new version is available: " + EnumChatFormatting.AQUA + thread.latestVersion.replace(Loader.MC_VERSION, "") + EnumChatFormatting.WHITE));
                this.alreadyDisplayed = true;
                FMLCommonHandler.instance().bus().unregister(this);
            }
        }
    }

    public static class UThread extends Thread {

        private final IUpdateCheck update;
        private boolean updateAvailable = false;
        private boolean checkComplete = false;
        private String latestVersion;

        public UThread(IUpdateCheck update) {
            super(update.getModId() + "UpdateCheck");
            this.update = update;
        }

        @Override
        public void run() {
            super.run();
            try {
                URL u = new URL(this.update.getUpdateUrl());
                BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
                List<String> lines = IOUtils.readLines(in);
                this.latestVersion = lines.get(0).split("\\s+")[0];
                INpureCore.proxy.print("Local: " + this.update.getVersion() + ", Remote: " + this.latestVersion);
                if (!latestVersion.equals(this.update.getVersion())) {
                    this.updateAvailable = true;
                    INpureCore.proxy.print("Update found for " + this.update.getModName());
                } else {
                    INpureCore.proxy.print("No update found for " + this.update.getModName());
                }
            } catch (Throwable t) {
                t.printStackTrace();
                checkComplete = false;
            }
            checkComplete = true;
        }
    }
}
