package cn.lanink.uuidcheck;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.Task;

public class EventListener implements Listener {

    private UUIDCheck uuidCheck;

    public EventListener(UUIDCheck uuidCheck) {
        this.uuidCheck = uuidCheck;
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.uuidCheck.getServer().getScheduler().scheduleDelayedTask(this.uuidCheck, new Task() {
            @Override
            public void onRun(int i) {
                if (player.isOnline()) {
                    String uuid = player.getUniqueId().toString();
                    Object object = uuidCheck.getSqlManager().getColumnValue(uuidCheck.getSqlManager().getConnection(),
                            "uuid", uuidCheck.titleName, "player", "'" + player.getName() + "'");
                    if (object != null) {
                        if (!object.equals(uuid)) {
                            player.kick(uuidCheck.getConfig().getString("踢出提示信息", "§cUUID校验失败！请联系管理！"));
                        }
                    }else {
                        //插入新数据
                        uuidCheck.getSqlManager().insertTableColumn(uuidCheck.getSqlManager().getConnection(),
                                uuidCheck.titleName, "player,uuid",
                                "'" + player.getName() + "','" + uuid +"'");
                    }
                }

            }
        }, 100, true);
    }

}
