package name.uuidcheck;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import name.uuidcheck.tasks.UUIDCheckTask;
import updata.AutoData;

import java.util.HashMap;

public class UUIDCheck extends PluginBase {

    private UUIDCheck uuidCheck;
    private Config config, player;
    private HashMap<String, String> players = new HashMap<>();

    @Override
    public void onEnable() {
        if (this.uuidCheck == null) this.uuidCheck = this;
        if (getServer().getPluginManager().getPlugin("AutoUpData") != null) {
            getLogger().info("§e 检查更新中...");
            if (AutoData.defaultUpData(this, getFile(), "lt-name", "UUIDCheck_Nukkit")) {
                return;
            }
        }
        saveDefaultConfig();
        this.config = new Config(getDataFolder()+ "/config.yml", 2);
        this.player = new Config(getDataFolder()+ "/player.yml", 2);
        if (this.player.get("Player") == null) {
            this.player.set("Player", new HashMap<>());
        }
        this.players = (HashMap<String, String>) this.player.get("Player");
        getServer().getScheduler().scheduleRepeatingTask(
                this, new UUIDCheckTask(this), 100, true);
        getLogger().info("§a 加载完成！");
    }

    @Override
    public void onDisable() {
        this.player.set("Player", this.players);
        this.player.save();
        getLogger().info("§c 已卸载！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("ucheck")) {
            if (sender instanceof Player) {
                sender.sendMessage("§c请使用控制台执行命令！");
            }else {
                if (args.length > 0) {
                    switch (args[0]) {
                        case "set":
                            if (args.length > 2) {
                                this.players.put(args[1], args[2]);
                                this.player.set("Player", this.players);
                                this.player.save();
                                sender.sendMessage("已设置玩家：" + args[1] + "的UUID为：" + args[2]);
                            }else {
                                sender.sendMessage("使用方法：/ucheck set 玩家名称 UUID");
                            }
                            break;
                        case "del":
                            if (args[1] != null) {
                                this.players.remove(args[1]);
                                this.player.set("Player", this.players);
                                this.player.save();
                                sender.sendMessage("玩家：" + args[1] + "的记录已删除！");
                            }else {
                                sender.sendMessage("使用方法: /ucheck del 玩家名称");
                            }
                            break;
                        default:
                            sender.sendMessage("§e==UUIDCheck命令帮助==");
                            sender.sendMessage("§a设置记录：/ucheck set 玩家名称 UUID");
                            sender.sendMessage("§a删除记录：/ucheck del 玩家名称");
                            sender.sendMessage("§a注意：玩家名称有空格请加上双引号！");
                            break;
                    }
                }else {
                    sender.sendMessage("/ucheck help 查看帮助！");
                }
            }
            return true;
        }
        return false;
    }

    public UUIDCheck getInstance() {
        return this.uuidCheck;
    }

    public HashMap<String, String> getPlayers() {
        return this.players;
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    public Config getPlayer() {
        return this.player;
    }

}
