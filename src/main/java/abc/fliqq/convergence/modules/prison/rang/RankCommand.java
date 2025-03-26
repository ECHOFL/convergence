package abc.fliqq.convergence.modules.prison.rang;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.commands.Command;
import abc.fliqq.convergence.core.commands.SubCommand;
import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.rang.connection.PlayerRankDataService;
import abc.fliqq.convergence.modules.prison.rang.manager.MineRankManager;
import abc.fliqq.convergence.modules.prison.rang.manager.PlayerRankCacheManager;

/**
 * Commande d'administration pour gérer le rang et le prestige d'un joueur.
 * Permet d'afficher ("info") et de modifier ("set") le rang.
 */
public class RankCommand extends Command {

    private final PlayerRankDataService playerRankDataService;
    private final MineRankManager mineRankManager;
    private final PlayerRankCacheManager playerRankCacheManager;
    
    public RankCommand(Convergence plugin, PrisonModule module) {
        super(plugin, "rank", "convergence.rank.admin", true);
        this.playerRankDataService = module.getPlayerRankDataService();
        this.mineRankManager = module.getMineRankManager();
        this.playerRankCacheManager = module.getPlayerRankCacheManager();
        
        setDescription("Administer player ranks and prestige");
        setUsage("/rank <subcommand>");
        
        registerSubCommands();
    }
    
    private void registerSubCommands() {
        addSubCommand(new InfoSubCommand());
        addSubCommand(new SetSubCommand());
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        plugin.getMessageService().sendMessage(sender, "general.invalid-command", Map.of("command", "rank"));
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        sender.sendMessage(plugin.getMessageService().colorize("&b&lRank Admin Commands:"));
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/rank info <player> &8- &7Display player's rank and prestige"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/rank set <player> <rank> <prestige> &8- &7Set player's rank and prestige"));
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
    }
    
    /**
     * Sous-commande "info" pour afficher le rang et le prestige d'un joueur.
     */
    private class InfoSubCommand extends SubCommand {
        public InfoSubCommand() {
            super("info", "convergence.rank.info", false);
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", Map.of("command", "rank info <player>"));
                return true;
            }
            
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.getMessageService().sendMessage(sender, "general.player-not-found", Map.of("player", args[0]));
                return true;
            }
            UUID playerId = target.getUniqueId();
            // Récupérer depuis le cache
            PlayerRank rank = playerRankCacheManager.getPlayerRank(playerId);
            if (rank == null) {
                plugin.getMessageService().sendMessage(sender, "prison.rank.not-found", Map.of("player", target.getName()));
            } else {
                plugin.getMessageService().sendMessage(sender, "prison.rank.info", 
                        Map.of("player", target.getName(), "rank", rank.getMineRank().getName(), "prestige", String.valueOf(rank.getPrestigeLevel())));
            }
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande "set" pour modifier manuellement le rang et le niveau de prestige d'un joueur.
     */
    private class SetSubCommand extends SubCommand {
        public SetSubCommand() {
            super("set", "convergence.rank.set", true);
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", Map.of("command", "rank set <player> <rank> <prestige>"));
                return true;
            }
            
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null) {
                plugin.getMessageService().sendMessage(sender, "general.player-not-found", Map.of("player", args[0]));
                return true;
            }
            UUID playerId = target.getUniqueId();
            
            String inputRank = args[1];
            // On récupère le MineRanks correspondant via le MineRankManager
            MineRanks newRank = mineRankManager.getMineRankFromId(inputRank);
            if(newRank == null) {
                plugin.getMessageService().sendMessage(sender, "prison.rank.invalid-rank", Map.of("rank", inputRank));
                return true;
            }
            
            int prestige;
            try {
                prestige = Integer.parseInt(args[2]);
                if(prestige < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                plugin.getMessageService().sendMessage(sender, "prison.rank.invalid-prestige", Map.of("prestige", args[2]));
                return true;
            }
            
            // Mise à jour dans le cache
            PlayerRank rank = playerRankCacheManager.getPlayerRank(playerId);
            if(rank == null) {
                // Si aucune donnée en cache, on initialise une entrée par défaut et la modifie ensuite.
                try {
                    playerRankDataService.initializePlayerRank(playerId, newRank, prestige);
                    rank = playerRankDataService.loadPlayerRank(playerId);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return true;
                }
            } else {
                // Mettre à jour les valeurs
                rank.setMineRank(newRank);
                rank.setPrestigeLevel(prestige);
            }
            playerRankCacheManager.putPlayerRank(playerId, rank);

            plugin.getMessageService().sendMessage(sender, "prison.rank.set-success", Map.of("player", target.getName(), "rank", newRank.getName(), "prestige", String.valueOf(prestige)));
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            // Pour le nom du joueur et le rang (on peut proposer les clés disponibles)
            if(args.length == 1) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            } else if(args.length == 2) {
                // On retourne la liste des identifiants de rang disponible
                return mineRankManager.getMineRanks().stream()
                        .map(MineRanks::getName)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
}