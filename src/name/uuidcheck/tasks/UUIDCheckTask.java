package name.uuidcheck.tasks;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import name.uuidcheck.UUIDCheck;

public class UUIDCheckTask extends PluginTask<UUIDCheck> {

    public UUIDCheckTask(UUIDCheck owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (Player player : owner.getServer().getOnlinePlayers().values()) {
            if (owner.getPlayers().containsKey(player.getName())) {
                if (!owner.getPlayers().get(player.getName()).equals(player.getUniqueId().toString())) {
                    player.kick(owner.getConfig().getString("踢出提示信息", "§cUUID校验失败！请联系管理！"));
                }
            }else {
                owner.getPlayers().put(player.getName(), player.getUniqueId().toString());
                owner.getPlayer().set("Player", owner.getPlayers());
                owner.getPlayer().save();
            }
        }
    }
}
