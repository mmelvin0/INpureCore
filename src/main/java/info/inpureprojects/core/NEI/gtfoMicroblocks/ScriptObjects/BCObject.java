package info.inpureprojects.core.NEI.gtfoMicroblocks.ScriptObjects;

import codechicken.nei.api.API;
import info.inpureprojects.core.INpureCore;
import info.inpureprojects.core.NEI.gtfoMicroblocks.NEIINpureConfig;
import net.minecraft.item.ItemStack;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * Created by den on 10/29/2014.
 */
public class BCObject {

    public BCObject() {
        NEIINpureConfig.logger.debug("Setting up BuildCraft Library...");
    }

    public void obliterate_facades(int index) {
        NEIINpureConfig.logger.debug("obliterate_microblocks called (version in %s). Params: %s", this.getClass().getName(), String.valueOf(index));
        try {
            AbstractList<ItemStack> facades = (AbstractList<ItemStack>)Class.forName("buildcraft.transport.ItemFacade").getDeclaredField("allFacades").get(null);
            API.setItemListEntries(facades.get(0).getItem(), Arrays.asList(new ItemStack[]{facades.get(index)}));
        } catch (Throwable t) {
            INpureCore.proxy.warning("Failed to hook bc!");
            t.printStackTrace();
        }
    }

    public int getFacadesSize() {
        try {
            AbstractList<ItemStack> facades = (AbstractList<ItemStack>)Class.forName("buildcraft.transport.ItemFacade").getDeclaredField("allFacades").get(null);
            NEIINpureConfig.logger.debug("getFacadesSize called. Returned: %s", String.valueOf(facades.size()));
            return facades.size();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

}
