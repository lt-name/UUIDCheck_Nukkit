package cn.lanink.uuidcheck;

import cn.lanink.uuidcheck.tasks.UUIDCheckTask;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.easysql.api.SqlEnable;
import com.smallaswater.easysql.mysql.manager.SqlManager;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDCheck extends PluginBase {

    private static UUIDCheck uuidCheck;
    private Config config, players;
    private SqlEnable sqlEnable;
    private SqlManager sqlManager;
    public String titleName;
    public ConcurrentHashMap<String, String> dataCache = new ConcurrentHashMap<>();
    public boolean useMySQL = false;

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
        this.useMySQL = (boolean) sqlConfig.get("use");
        if (this.useMySQL) {
            getLogger().info("§a已启用数据库，正在尝试连接，请稍后...");
            this.sqlEnable = new SqlEnable(this, this.titleName, new UserData(
                    (String) sqlConfig.get("user"),
                    (String) sqlConfig.get("passWorld"),
                    (String) sqlConfig.get("host"),
                    (Integer) sqlConfig.get("port"),
                    (String) sqlConfig.get("database")),
                    new TableType("player", Types.VARCHAR),
                    new TableType("uuid", Types.VARCHAR));
            this.sqlManager = this.sqlEnable.getManager();
        }else {
            getLogger().info("§a未启用数据库，正常读取记录文件...");
            this.players = new Config(getDataFolder() + "/players.yml", 2);
            for (Map.Entry<Object, Object> entry : this.players.get("players", new HashMap<>()).entrySet()) {
                this.dataCache.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        if (this.config.getBoolean("使用异步task检测", false)) {
            getServer().getScheduler().scheduleRepeatingTask(
                    this, new UUIDCheckTask(this),
                    this.config.getInt("异步检测间隔", 100), true);
        }
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getLogger().info("§a加载完成！");
    }

    @Override
    public void onDisable() {
        if (this.useMySQL) {
            this.sqlEnable.disable();
        }else if (this.players != null && this.dataCache.size() > 0) {
            this.players.set("players", this.dataCache);
            this.players.save();
        }
        getServer().getScheduler().cancelTask(this);
        getLogger().info("§c已卸载！");
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
                                if (!args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                                    sender.sendMessage(args[2] + " 这不是正确的uuid！");
                                    return true;
                                }
                                Object object;
                                if (this.useMySQL) {
                                    object = this.sqlManager.getColumnValue(this.sqlManager.getConnection(),
                                            "uuid", this.titleName, "player", "'" + args[1] + "'");
                                }else {
                                    object = this.dataCache.get(args[1]);
                                }
                                if (object != null) {
                                    this.dataCache.put(args[1], args[2]);
                                    if (this.useMySQL) {
                                        this.sqlManager.upDataTableColumn(this.sqlManager.getConnection(),
                                                this.titleName, "uuid", "'" + args[2] + "'", "player", "'" + args[1] + "'");
                                    }else {
                                        this.players.set("players", this.dataCache);
                                        this.players.save();
                                    }
                                    sender.sendMessage("已设置玩家：" + args[1] + "的UUID记录为：" + args[2]);
                                }else {
                                    sender.sendMessage("数据库中没有玩家：" + args[1] + " 的记录！");
                                    return true;
                                }
                            }else {
                                sender.sendMessage("使用方法：/ucheck set 玩家名称 UUID");
                            }
                            break;
                        case "del":
                            if (args[1] != null) {
                                if (this.useMySQL) {
                                    if (this.sqlManager.isTableColumnData(this.sqlManager.getConnection(),
                                            "player", this.titleName, "player", "'" + args[1] + "'")) {
                                        try {
                                            PreparedStatement preparedStatement = this.sqlManager.getConnection().prepareStatement(
                                                    "delete from " + this.titleName + " where player = ?");
                                            preparedStatement.setString(1, args[1]);
                                            preparedStatement.execute();
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        }
                                        this.dataCache.remove(args[1]);
                                        sender.sendMessage("玩家：" + args[1] + " 的记录已删除！");
                                    }else {
                                        sender.sendMessage("数据库中本来就没有玩家：" + args[1] + " 的记录！");
                                    }
                                }else {
                                    if (this.dataCache.containsKey(args[1])) {
                                        this.dataCache.remove(args[1]);
                                        this.players.set("players", this.dataCache);
                                        sender.sendMessage("玩家：" + args[1] + " 的记录已删除！");
                                    }else {
                                        sender.sendMessage("数据库中本来就没有玩家：" + args[1] + " 的记录！");
                                    }
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

    public Config getPlayers() {
        return this.players;
    }

    public SqlManager getSqlManager() {
        return this.sqlManager;
    }

}
