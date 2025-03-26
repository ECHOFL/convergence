package abc.fliqq.convergence.modules.prison.mine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.commands.Command;
import abc.fliqq.convergence.core.commands.SubCommand;
import abc.fliqq.convergence.modules.prison.PrisonModule;

/**
 * Commande pour la gestion des mines de rang.
 */
public class MineCommand extends Command {
    
    private final MineManager mineManager;
    
    /**
     * Constructeur de la commande mine
     * 
     * @param plugin L'instance du plugin
     * @param module Le module prison
     */
    public MineCommand(Convergence plugin, PrisonModule module) {
        super(plugin, "mine", "convergence.mine", false);
        this.mineManager = module.getMineManager();
        
        setDescription("Manage prison mines (RANK only)");
        setUsage("/mine <subcommand>");
        
        // Enregistrer les sous-commandes
        registerSubCommands();
    }
    
    /**
     * Enregistre tous les sous-commandes
     */
    private void registerSubCommands() {
        addSubCommand(new CreateSubCommand());
        addSubCommand(new DeleteSubCommand());
        addSubCommand(new InfoSubCommand());
        addSubCommand(new ListSubCommand());
        addSubCommand(new TeleportSubCommand());
        addSubCommand(new SetPosSubCommand());
        addSubCommand(new ResetSubCommand());
        addSubCommand(new CompositionSubCommand());
        addSubCommand(new ReloadSubCommand());
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Afficher l'aide
            showHelp(sender);
            return true;
        }
        
        plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                Map.of("command", "mine"));
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
    
    /**
     * Affiche le message d'aide
     * 
     * @param sender Le CommandSender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        sender.sendMessage(plugin.getMessageService().colorize("&b&lMine Commands (RankMine):"));
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        
        if (sender.hasPermission("convergence.mine.create")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine create <id> <name> &8- &7Create a new mine (Rank required equals id)"));
        }
        
        if (sender.hasPermission("convergence.mine.delete")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine delete <id> &8- &7Delete a mine"));
        }
        
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine info <id> &8- &7View mine information"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine list &8- &7List all mines"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine tp <id> &8- &7Teleport to a mine"));
        
        if (sender.hasPermission("convergence.mine.setpos")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine setpos <id> <1|2> &8- &7Set mine region position"));
        }
        
        if (sender.hasPermission("convergence.mine.reset")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine reset <id> &8- &7Reset a mine"));
        }
        
        if (sender.hasPermission("convergence.mine.composition")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine composition <id> <material> <percentage> &8- &7Set mine composition"));
        }
        
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine reload &8- &7Reload mines configuration"));
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
    }
    
    /**
     * Sous-commande pour créer une mine (RankOnly)
     */
    private class CreateSubCommand extends SubCommand {
        
        public CreateSubCommand() {
            super("create", "convergence.mine.create", false);
            setAliases("new", "add");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine create <id> <name>"));
                return true;
            }
            
            // L'argument 0 correspond à l'id qui représente également le rank (de A à Z)
            String id = args[0];
            
            // Vérifier si la mine existe déjà
            if (mineManager.getMine(id) != null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.already-exists", 
                        Map.of("name", id));
                return true;
            }
            
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                nameBuilder.append(args[i]).append(" ");
            }
            String name = nameBuilder.toString().trim();
            
            // Créer la RankMine : ici le requiredRank est défini comme l'id (supposé être une lettre)
            Mine mine = mineManager.createRankMine(id, name, id, 300);
            if (mine != null) {
                mineManager.saveMines();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", name);
                placeholders.put("id", id);
                plugin.getMessageService().sendMessage(sender, "prison.mine.created", placeholders);
            }
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour supprimer une mine
     */
    private class DeleteSubCommand extends SubCommand {
        
        public DeleteSubCommand() {
            super("delete", "convergence.mine.delete", false);
            setAliases("remove");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine delete <id>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            mineManager.getRankMines().remove(id);
            mineManager.saveMines();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", mine.getName());
            placeholders.put("id", id);
            plugin.getMessageService().sendMessage(sender, "prison.mine.deleted", placeholders);
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour afficher les informations d'une mine
     */
    private class InfoSubCommand extends SubCommand {
        
        public InfoSubCommand() {
            super("info", "", false);
            setAliases("view");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine info <id>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&b&lMine Information:"));
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&bID: &7" + mine.getId()));
            sender.sendMessage(plugin.getMessageService().colorize("&bName: &7" + mine.getName()));
            sender.sendMessage(plugin.getMessageService().colorize("&bReset Interval: &7" + mine.getResetInterval() + " seconds"));
            sender.sendMessage(plugin.getMessageService().colorize("&bRequired Rank: &7" + mine.getRequiredRank()));
            
            if (mine.hasValidRegion()) {
                Location pos1 = mine.getPos1();
                Location pos2 = mine.getPos2();
                sender.sendMessage(plugin.getMessageService().colorize("&bRegion: &7" + 
                        pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ() + " to " +
                        pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ()));
                int xSize = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
                int ySize = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
                int zSize = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
                int volume = xSize * ySize * zSize;
                sender.sendMessage(plugin.getMessageService().colorize("&bVolume: &7" + volume + " blocks (" + 
                        xSize + "x" + ySize + "x" + zSize + ")"));
            } else {
                sender.sendMessage(plugin.getMessageService().colorize("&bRegion: &cNot set"));
            }
            
            sender.sendMessage(plugin.getMessageService().colorize("&bComposition:"));
            if (mine.getComposition().isEmpty()) {
                sender.sendMessage(plugin.getMessageService().colorize("  &7None"));
            } else {
                for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
                    sender.sendMessage(plugin.getMessageService().colorize("  &7" + entry.getKey().name() + ": &b" + entry.getValue() + "%"));
                }
            }
            
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour lister les mines
     */
    private class ListSubCommand extends SubCommand {
        
        public ListSubCommand() {
            super("list", "", false);
            setAliases("ls");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            List<Mine> mines = new ArrayList<>(mineManager.getRankMines().values());
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&b&lMine List:"));
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            if (mines.isEmpty()) {
                sender.sendMessage(plugin.getMessageService().colorize("&7No mines found."));
            } else {
                for (Mine mine : mines) {
                    String status = mine.hasValidRegion() ? "&a✓" : "&c✗";
                    sender.sendMessage(plugin.getMessageService().colorize("&b" + mine.getId() + " &8- &7" + mine.getName() + " " + status));
                }
            }
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
        }
    }
    
    /**
     * Sous-commande pour téléporter vers une mine
     */
    private class TeleportSubCommand extends SubCommand {
        
        public TeleportSubCommand() {
            super("teleport", "", true);
            setAliases("tp");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine tp <id>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            if (!mine.hasValidRegion()) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.no-region", 
                        Map.of("name", mine.getName()));
                return true;
            }
            
            Player player = (Player) sender;
            Location pos1 = mine.getPos1();
            Location pos2 = mine.getPos2();
            Location center = new Location(
                    pos1.getWorld(),
                    (pos1.getX() + pos2.getX()) / 2,
                    Math.max(pos1.getY(), pos2.getY()) + 1,
                    (pos1.getZ() + pos2.getZ()) / 2);
            player.teleport(center);
            plugin.getMessageService().sendMessage(sender, "prison.mine.teleported", 
                    Map.of("name", mine.getName()));
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour définir les positions de la région d'une mine
     */
    private class SetPosSubCommand extends SubCommand {
        
        public SetPosSubCommand() {
            super("setpos", "convergence.mine.setpos", true);
            setAliases("pos");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine setpos <id> <1|2>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            int pos;
            try {
                pos = Integer.parseInt(args[1]);
                if (pos != 1 && pos != 2) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-position", 
                        Map.of("position", args[1]));
                return true;
            }
            
            Player player = (Player) sender;
            Location location = player.getLocation().getBlock().getLocation();
            if (pos == 1) {
                mine.setPos1(location);
                plugin.getMessageService().sendMessage(sender, "prison.mine.set-pos1", 
                        Map.of("name", mine.getName()));
            } else {
                mine.setPos2(location);
                plugin.getMessageService().sendMessage(sender, "prison.mine.set-pos2", 
                        Map.of("name", mine.getName()));
            }
            mineManager.saveMines();
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            } else if (args.length == 2) {
                return Arrays.asList("1", "2");
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour réinitialiser une mine
     */
    private class ResetSubCommand extends SubCommand {
        
        public ResetSubCommand() {
            super("reset", "convergence.mine.reset", false);
            setAliases("r");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine reset <id>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            if (!mine.hasValidRegion()) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.no-region", 
                        Map.of("name", mine.getName()));
                return true;
            }
            
            mineManager.resetMine(mine);
            plugin.getMessageService().sendMessage(sender, "prison.mine.reset", 
                    Map.of("name", mine.getName()));
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour définir la composition d'une mine
     */
    private class CompositionSubCommand extends SubCommand {
        
        public CompositionSubCommand() {
            super("composition", "convergence.mine.composition", false);
            setAliases("comp");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine composition <id> <material> <percentage>"));
                return true;
            }
            
            String id = args[0];
            Mine mine = mineManager.getMine(id);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            Material material;
            try {
                material = Material.valueOf(args[1].toUpperCase());
                if (!material.isBlock()) {
                    plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-block", 
                            Map.of("material", args[1]));
                    return true;
                }
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-material", 
                        Map.of("material", args[1]));
                return true;
            }
            
            double percentage;
            try {
                percentage = Double.parseDouble(args[2]);
                if (percentage < 0 || percentage > 100) {
                    plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-percentage", 
                            Map.of("percentage", args[2]));
                    return true;
                }
            } catch (NumberFormatException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-percentage", 
                        Map.of("percentage", args[2]));
                return true;
            }
            
            mine.addMaterial(material, percentage);
            double total = mine.getComposition().values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(total - 100.0) > 0.01) {
                sender.sendMessage(plugin.getMessageService().colorize("&eWarning: Total composition is " + 
                        String.format("%.2f", total) + "%. Consider adjusting to total 100%."));
            }
            mineManager.saveMines();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", mine.getName());
            placeholders.put("material", material.name());
            placeholders.put("percentage", String.format("%.2f", percentage));
            plugin.getMessageService().sendMessage(sender, "prison.mine.composition-updated", placeholders);
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return mineManager.getRankMines().keySet().stream().collect(Collectors.toList());
            } else if (args.length == 2) {
                return Arrays.stream(Material.values())
                        .filter(Material::isBlock)
                        .filter(m -> !m.name().contains("LEGACY"))
                        .map(Material::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                return Arrays.asList("5", "10", "15", "20", "25", "30", "50", "75", "100");
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Sous-commande pour recharger la configuration des mines
     */
    private class ReloadSubCommand extends SubCommand {

        public ReloadSubCommand() {
            super("reload", "convergence.mine.reload", false);
        }
    
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            mineManager.reload();
            plugin.getMessageService().sendMessage(sender, "prison.mine.reload-success");
            return true;
        }
    
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }
}

