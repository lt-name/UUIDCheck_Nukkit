package cn.lanink.uuidcheck.tasks;

import cn.lanink.uuidcheck.UUIDCheck;
import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

public class UUIDCheckTask extends PluginTask<UUIDCheck> {

    private boolean use = false;

    public UUIDCheckTask(UUIDCheck owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        if (use) return;
        use = true;
        for (Player player : owner.getServer().getOnlinePlayers().values()) {
            String uuid = player.getUniqueId().toString();
            if (owner.dataCache.containsKey(player.getName())) {
                if (!owner.dataCache.get(player.getName()).equals(uuid)) {
                    player.kick(owner.getConfig().getString("踢出提示信息", "§cUUID校验失败！请联系管理！"));
                }
            }else if (owner.useMySQL) {
                Object object = owner.getSqlManager().getColumnValue(owner.getSqlManager().getConnection(),
                        "uuid", owner.titleName, "player", "'" + player.getName() + "'");
                if (object != null) {
                    if (object.equals(uuid)) {
                        owner.dataCache.put(player.getName(), (String) object);
                    }else {
                        player.kick(owner.getConfig().getString("踢出提示信息", "§cUUID校验失败！请联系管理！"));
                    }
                }
            }
        }
        use = false;
    }
}
