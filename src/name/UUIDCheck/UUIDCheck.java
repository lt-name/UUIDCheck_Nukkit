package name.UUIDCheck;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import name.UUIDCheck.Tasks.UUIDCheckTask;

import java.util.LinkedHashMap;

public class UUIDCheck extends PluginBase {

    private UUIDCheck uuidCheck;
    private Config config;
    private LinkedHashMap<String, String> players = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        this.uuidCheck = this;
        saveDefaultConfig();
        this.config = new Config(getDataFolder()+ "/config.yml", 2);
        this.players = (LinkedHashMap<String, String>) this.config.get("Player");
        getServer().getScheduler().scheduleRepeatingTask(
                this, new UUIDCheckTask(this), 20, true);
        getLogger().info("§a 加载完成！");
    }

    @Override
    public void onDisable() {
        this.config.set("Player", this.players);
        this.config.save();
        getLogger().info("§c 已卸载！");
    }

    public UUIDCheck getInstance() {
        return this.uuidCheck;
    }

    public LinkedHashMap<String, String> getPlayers() {
        return this.players;
    }

    public Config getConfig() {
        return this.config;
    }

}
