package cn.lanink.uuidcheck;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.scheduler.Task;

public class EventListener implements Listener {

    private final UUIDCheck uuidCheck;

    public EventListener(UUIDCheck uuidCheck) {
        this.uuidCheck = uuidCheck;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.uuidCheck.getServer().getScheduler().scheduleDelayedTask(this.uuidCheck, new Task() {
            @Override
            public void onRun(int i) {
                if (player != null && player.isOnline()) {
                    String uuid = player.getUniqueId().toString();
                    Object object;
                    if (uuidCheck.useMySQL) {
                        object = uuidCheck.getSqlManager().getColumnValue(uuidCheck.getSqlManager().getConnection(),
                                "uuid", uuidCheck.titleName, "player", "'" + player.getName() + "'");
                    }else {
                        object = uuidCheck.dataCache.get(player.getName());
                    }
                    if (object != null) {
                        if (object.equals(uuid)) {
                            if (uuidCheck.useMySQL) {
                                uuidCheck.dataCache.put(player.getName(), uuid);
                            }
                        }else {
                            player.kick(uuidCheck.getConfig().getString("踢出提示信息", "§cUUID校验失败！请联系管理！"));
                        }
                    }else {
                        if (uuidCheck.useMySQL) {
                            uuidCheck.getSqlManager().insertTableColumn(uuidCheck.getSqlManager().getConnection(),
                                    uuidCheck.titleName, "player,uuid", "'" + player.getName() + "','" + uuid +"'");
                        }else {
                            uuidCheck.dataCache.put(player.getName(), uuid);
                            uuidCheck.getPlayers().set("players", uuidCheck.dataCache);
                            uuidCheck.getPlayers().save();
                        }
                    }
                }
            }
        }, 10);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null && uuidCheck.useMySQL) {
            this.uuidCheck.dataCache.remove(player.getName());
        }
    }

}