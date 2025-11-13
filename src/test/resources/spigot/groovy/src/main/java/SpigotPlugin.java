import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SpigotPlugin extends SpigotBasePlugin {
    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTask(this);
    }
}