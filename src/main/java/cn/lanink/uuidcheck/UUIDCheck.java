package cn.lanink.uuidcheck;

import cn.lanink.uuidcheck.tasks.UUIDCheckTask;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.easysql.api.manager.SqlManager;
import com.smallaswater.easysql.api.mysql.utils.TableType;
import com.smallaswater.easysql.api.mysql.utils.Types;
import com.smallaswater.easysql.api.mysql.utils.UserData;

import java.util.HashMap;

public class UUIDCheck extends PluginBase {

    private static UUIDCheck uuidCheck;
    private Config config;
    private SqlManager sqlManager;
    public String titleName;

    public static UUIDCheck getInstance() {
        return uuidCheck;
    }

    @Override
    public void onEnable() {
        if (uuidCheck == null) uuidCheck = this;
        saveDefaultConfig();
        this.config = new Config(getDataFolder() + "/config.yml", 2);
        this.titleName = "uuidcheck";
        //连接数据库
        HashMap<String, Object> sqlConfig = this.config.get("MySQL", new HashMap<>());
        this.sqlManager = new SqlManager(this, this.titleName, new UserData(
                (String) sqlConfig.get("user"),
                (String) sqlConfig.get("passWorld"),
                (String) sqlConfig.get("host"),
                (Integer) sqlConfig.get("port"),
                (String) sqlConfig.get("table")),
                new TableType("player", Types.VARCHAR),
                new TableType("uuid", Types.VARCHAR));
        if (this.config.getBoolean("使用异步task检测", false)) {
            getServer().getScheduler().scheduleRepeatingTask(
                    this, new UUIDCheckTask(this),
                    this.config.getInt("异步检测间隔", 100), true);
        }
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getLogger().info("§a 加载完成！");
    }

    @Override
    public void onDisable() {
        this.sqlManager.shutdown();
        getServer().getScheduler().cancelTask(this);
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
                                Object object = this.sqlManager.getColumnValue(this.sqlManager.getConnection(),
                                        "uuid", this.titleName, "player", "'" + args[1] + "'");
                                if (object != null) {
                                    this.sqlManager.upDataTableColumn(this.sqlManager.getConnection(),
                                            this.titleName, "uuid", "'" + args[2] + "'", "player", "'" + args[1] + "'");
                                }else {
                                    sender.sendMessage("数据库中没有玩家：" + args[1] + " 的记录！");
                                    return true;
                                }
                                sender.sendMessage("已设置玩家：" + args[1] + "的UUID记录为：" + args[2]);
                            }else {
                                sender.sendMessage("使用方法：/ucheck set 玩家名称 UUID");
                            }
                            break;
                        case "del":
                            if (args[1] != null) {
                                if (this.sqlManager.isTableColumnData(this.sqlManager.getConnection(),
                                        "player", this.titleName, "player", "'" + args[1] + "'")) {
                                    this.sqlManager.execute(this.sqlManager.getConnection(),
                                            "DELETE FROM " + this.titleName + " WHERE player = '" + args[1] + "'");
                                    sender.sendMessage("玩家：" + args[1] + " 的记录已删除！");
                                }else {
                                    sender.sendMessage("数据库中本来就没有玩家：" + args[1] + " 的记录！");
                                }
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

    @Override
    public Config getConfig() {
        return this.config;
    }

    public SqlManager getSqlManager() {
        return this.sqlManager;
    }

}
