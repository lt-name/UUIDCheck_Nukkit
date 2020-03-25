package name.UUIDCheck.Tasks;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import name.UUIDCheck.UUIDCheck;

public class UUIDCheckTask extends PluginTask<UUIDCheck> {

    public UUIDCheckTask(UUIDCheck owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (Player player : owner.getServer().getOnlinePlayers().values()) {
            if (owner.getPlayers().containsKey(player.getName())) {
                if (!owner.getPlayers().get(player.getName()).equals(player.getUniqueId().toString())) {
                    player.kick("§cUUID校验失败！请联系管理！");
                }
            }else {
                owner.getPlayers().put(player.getName(), player.getUniqueId().toString());
                owner.getConfig().set("Player", owner.getPlayers());
                owner.getConfig().save();
            }
        }
    }
}
