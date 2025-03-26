package abc.fliqq.convergence.modules.prison.custompickaxe.listener;

import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.connection.PlayerEnchantDataService;
import abc.fliqq.convergence.modules.prison.custompickaxe.manager.EnchantsManager;

public class PlayerConnectionListener implements Listener{

    private final PlayerEnchantDataService playerEnchantDataService;
    private final EnchantsManager enchantsManager;

    public PlayerConnectionListener(PrisonModule module){
        playerEnchantDataService = module.getPlayerEnchantDataService();
        enchantsManager = module.getEnchantsManager();
    }
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) throws SQLException{
        Player player = e.getPlayer();
        playerEnchantDataService.ensurePlayerEnchantRow(player.getUniqueId().toString(), 
            enchantsManager.getEnchants().keySet());
    }
    
}
