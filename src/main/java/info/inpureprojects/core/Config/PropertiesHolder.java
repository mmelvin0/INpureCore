package info.inpureprojects.core.Config;

import info.inpureprojects.core.INpureCore;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.lang.reflect.Field;

/**
 * Created by den on 7/26/2014.
 */
public class PropertiesHolder {

    private static final String remove = "removed_options_please_ignore";

    @Option(category = "tweaks", key = "Shut_Up_Missing_Texture_Spam", comment = "MISSING TEXTURE BLAH/THISPATHISNONSENSE/SHUTUPMINECRAFT. Be gone!")
    public boolean textureLoggerOverride = false;
    @DeprecatedOption(category = "tweaks", key = "Hide_Microblocks_From_NEI")
    public boolean hideCrapFromNEI = false;
    @DeprecatedOption(category = "tweaks", key = "Hide_VanillaCrap_From_NEI")
    public boolean hideVanillaCrapFromNEI = false;
    @Option(category = "updater", key = "check")
    public boolean updateCheck = false;
    @Option(category = "scripting", key = "extract_scripts", comment = "Only extracts if the custom_nei_filters folder does not exist.")
    public boolean extract_scripts = false;
    @Option(category = "submodule", key = "silence_submodule_logging", comment = "Silences some debug output during load. Harmless.", value = false)
    public boolean silence_submodule_logging = false;
    @Option(category = "tweaks", key = "dump_registry_to_debug_log", comment = "Useful for making custom scripts.", value = false)
    public boolean dump_registry_to_debug_log = false;
    @Option(category = "tweaks", key = "complain_about_bad_names", comment = "Post one message per mod in the log if that mod is registering bad GameRegistry names.")
    public boolean complain_about_bad_names = true;

    public PropertiesHolder(Configuration config) {
        config.load();
        for (Field f : this.getClass().getDeclaredFields()) {
            if (f.getAnnotation(Option.class) != null) {
                Option o = f.getAnnotation(Option.class);
                if (!o.released()) {
                    continue;
                }
                Property p = config.get(o.category(), o.key(), o.value());
                if (!o.comment().equals("")) {
                    p.comment = o.comment();
                }
                try {
                    f.set(this, p.getBoolean());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (f.getAnnotation(DeprecatedOption.class) != null) {
                DeprecatedOption d = f.getAnnotation(DeprecatedOption.class);
                this.remove_option(config, d.category(), d.key());
            }
        }
        config.save();
    }

    private void remove_option(Configuration config, String category, String key) {
        if (config.hasKey(category, key)) {
            config.moveProperty(category, key, remove);
        }
        if (config.hasCategory("removed_options_please_ignore")) {
            INpureCore.proxy.print("Removing deprecated option " + key + " from config.");
            config.removeCategory(config.getCategory(remove));
        }
    }
}
