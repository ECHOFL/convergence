package abc.fliqq.convergence.modules.prison.mine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import abc.fliqq.convergence.Convergence;
import abc.fliqq.convergence.core.commands.Command;
import abc.fliqq.convergence.core.commands.SubCommand;
import abc.fliqq.convergence.modules.prison.PrisonModule;
import abc.fliqq.convergence.modules.prison.mine.Mine;
import abc.fliqq.convergence.modules.prison.mine.Mine.MineType;
import abc.fliqq.convergence.modules.prison.mine.MineManager;

/**
 * Command for managing mines
 */
public class MineCommand extends Command {
    
    private final PrisonModule module;
    private final MineManager mineManager;
    
    /**
     * Constructor for the mine command
     * 
     * @param plugin The plugin instance
     * @param module The prison module
     */
    public MineCommand(Convergence plugin, PrisonModule module) {
        super(plugin, "mine", "convergence.mine", false);
        this.module = module;
        this.mineManager = module.getMineManager();
        
        // Set command details
        setDescription("Manage prison mines");
        setUsage("/mine <subcommand>");
        
        // Register subcommands
        registerSubCommands();
    }
    
    /**
     * Registers all subcommands
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
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Show help
            showHelp(sender);
            return true;
        }
        
        // If we get here, the subcommand wasn't found
        plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                Map.of("command", "mine"));
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completions for the base command
        return new ArrayList<>();
    }
    
    /**
     * Shows the help message
     * 
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        sender.sendMessage(plugin.getMessageService().colorize("&b&lMine Commands:"));
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
        
        if (sender.hasPermission("convergence.mine.create")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine create <type> <id> <name> &8- &7Create a new mine"));
        }
        
        if (sender.hasPermission("convergence.mine.delete")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine delete <type> <id> &8- &7Delete a mine"));
        }
        
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine info <type> <id> &8- &7View mine information"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine list [type] &8- &7List all mines"));
        sender.sendMessage(plugin.getMessageService().colorize("&b/mine tp <type> <id> &8- &7Teleport to a mine"));
        
        if (sender.hasPermission("convergence.mine.setpos")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine setpos <type> <id> <1|2> &8- &7Set mine region position"));
        }
        
        if (sender.hasPermission("convergence.mine.reset")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine reset <type> <id> &8- &7Reset a mine"));
        }
        
        if (sender.hasPermission("convergence.mine.composition")) {
            sender.sendMessage(plugin.getMessageService().colorize("&b/mine composition <type> <id> <material> <percentage> &8- &7Set mine composition"));
        }
        
        sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
    }
    
    /**
     * Subcommand for creating a mine
     */
    private class CreateSubCommand extends SubCommand {
        
        public CreateSubCommand() {
            super("create", "convergence.mine.create", false);
            setAliases("new", "add");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine create <type> <id> <name>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Check if mine already exists
            if (mineManager.getMine(id, type) != null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.already-exists", 
                        Map.of("name", id));
                return true;
            }
            
            // Build name from remaining args
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                nameBuilder.append(args[i]).append(" ");
            }
            String name = nameBuilder.toString().trim();
            
            // Create the mine based on type
            Mine mine = null;
            switch (type) {
                case RANK:
                    mine = mineManager.createRankMine(id, name, id, 300);
                    break;
                case PRESTIGE:
                    try {
                        int prestigeLevel = Integer.parseInt(id);
                        mine = mineManager.createPrestigeMine(id, name, prestigeLevel, 300);
                    } catch (NumberFormatException e) {
                        plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-prestige", 
                                Map.of("id", id));
                        return true;
                    }
                    break;
                case PERSONAL:
                    if (!(sender instanceof Player)) {
                        plugin.getMessageService().sendMessage(sender, "general.player-only");
                        return true;
                    }
                    Player player = (Player) sender;
                    mine = mineManager.createPersonalMine(player.getUniqueId(), name, 300);
                    break;
            }
            
            if (mine != null) {
                // Save mines
                mineManager.saveMines();
                
                // Send success message
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", name);
                placeholders.put("type", type.name().toLowerCase());
                plugin.getMessageService().sendMessage(sender, "prison.mine.created", placeholders);
            }
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for deleting a mine
     */
    private class DeleteSubCommand extends SubCommand {
        
        public DeleteSubCommand() {
            super("delete", "convergence.mine.delete", false);
            setAliases("remove");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine delete <type> <id>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Delete the mine based on type
            boolean deleted = false;
            switch (type) {
                case RANK:
                    mineManager.getRankMines().remove(id);
                    deleted = true;
                    break;
                case PRESTIGE:
                    mineManager.getPrestigeMines().remove(id);
                    deleted = true;
                    break;
                case PERSONAL:
                    try {
                        mineManager.getPersonalMines().remove(java.util.UUID.fromString(id));
                        deleted = true;
                    } catch (IllegalArgumentException e) {
                        plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-uuid", 
                                Map.of("id", id));
                        return true;
                    }
                    break;
            }
            
            if (deleted) {
                // Save mines
                mineManager.saveMines();
                
                // Send success message
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", mine.getName());
                placeholders.put("type", type.name().toLowerCase());
                plugin.getMessageService().sendMessage(sender, "prison.mine.deleted", placeholders);
            }
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for viewing mine information
     */
    private class InfoSubCommand extends SubCommand {
        
        public InfoSubCommand() {
            super("info", "", false);
            setAliases("view");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine info <type> <id>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Display mine information
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&b&lMine Information:"));
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&bID: &7" + mine.getId()));
            sender.sendMessage(plugin.getMessageService().colorize("&bName: &7" + mine.getName()));
            sender.sendMessage(plugin.getMessageService().colorize("&bType: &7" + mine.getType().name()));
            sender.sendMessage(plugin.getMessageService().colorize("&bReset Interval: &7" + mine.getResetInterval() + " seconds"));
            
            // Type-specific information
            if (mine.isRankMine()) {
                sender.sendMessage(plugin.getMessageService().colorize("&bRequired Rank: &7" + mine.getRequiredRank()));
            } else if (mine.isPrestigeMine()) {
                sender.sendMessage(plugin.getMessageService().colorize("&bRequired Prestige: &7" + mine.getRequiredPrestige()));
            } else if (mine.isPersonalMine()) {
                sender.sendMessage(plugin.getMessageService().colorize("&bOwner: &7" + Bukkit.getOfflinePlayer(mine.getOwnerUUID()).getName()));
                sender.sendMessage(plugin.getMessageService().colorize("&bSize Level: &7" + mine.getSize()));
                sender.sendMessage(plugin.getMessageService().colorize("&bReset Speed Level: &7" + mine.getResetSpeed()));
                sender.sendMessage(plugin.getMessageService().colorize("&bComposition Level: &7" + mine.getCompositionLevel()));
            }
            
            // Region information
            if (mine.hasValidRegion()) {
                Location pos1 = mine.getPos1();
                Location pos2 = mine.getPos2();
                sender.sendMessage(plugin.getMessageService().colorize("&bRegion: &7" + 
                        pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ() + " to " +
                        pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ()));
                
                // Calculate volume
                int xSize = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
                int ySize = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
                int zSize = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
                int volume = xSize * ySize * zSize;
                
                sender.sendMessage(plugin.getMessageService().colorize("&bVolume: &7" + volume + " blocks (" + 
                        xSize + "x" + ySize + "x" + zSize + ")"));
            } else {
                sender.sendMessage(plugin.getMessageService().colorize("&bRegion: &cNot set"));
            }
            
            // Composition information
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
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for listing mines
     */
    private class ListSubCommand extends SubCommand {
        
        public ListSubCommand() {
            super("list", "", false);
            setAliases("ls");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            // Determine which type of mines to list
            MineType type = null;
            if (args.length > 0) {
                try {
                    type = MineType.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                            Map.of("type", args[0]));
                    return true;
                }
            }
            
            // Get mines to list
            List<Mine> mines;
            if (type != null) {
                mines = new ArrayList<>(mineManager.getMinesByType(type));
            } else {
                mines = new ArrayList<>(mineManager.getAllMines());
            }
            
            // Display mine list
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            sender.sendMessage(plugin.getMessageService().colorize("&b&lMine List:"));
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            
            if (mines.isEmpty()) {
                sender.sendMessage(plugin.getMessageService().colorize("&7No mines found."));
            } else {
                for (Mine mine : mines) {
                    String status = mine.hasValidRegion() ? "&a✓" : "&c✗";
                    sender.sendMessage(plugin.getMessageService().colorize("&b" + mine.getId() + " &8- &7" + mine.getName() + 
                            " &8(&7" + mine.getType().name() + "&8) " + status));
                }
            }
            
            sender.sendMessage(plugin.getMessageService().colorize("&8&m-----------------------------------------------------"));
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for teleporting to a mine
     */
    private class TeleportSubCommand extends SubCommand {
        
        public TeleportSubCommand() {
            super("teleport", "", true);
            setAliases("tp");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine tp <type> <id>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Check if mine has a valid region
            if (!mine.hasValidRegion()) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.no-region", 
                        Map.of("name", mine.getName()));
                return true;
            }
            
            // Teleport the player
            Player player = (Player) sender;
            Location pos1 = mine.getPos1();
            Location pos2 = mine.getPos2();
            
            // Calculate center of mine
            Location center = new Location(
                    pos1.getWorld(),
                    (pos1.getX() + pos2.getX()) / 2,
                    Math.max(pos1.getY(), pos2.getY()) + 1, // Teleport to top of mine
                    (pos1.getZ() + pos2.getZ()) / 2
            );
            
            player.teleport(center);
            
            // Send success message
            plugin.getMessageService().sendMessage(sender, "prison.mine.teleported", 
                    Map.of("name", mine.getName()));
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for setting mine positions
     */
    private class SetPosSubCommand extends SubCommand {
        
        public SetPosSubCommand() {
            super("setpos", "convergence.mine.setpos", true);
            setAliases("pos");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine setpos <type> <id> <1|2>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Parse position number
            int pos;
            try {
                pos = Integer.parseInt(args[2]);
                if (pos != 1 && pos != 2) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-position", 
                        Map.of("position", args[2]));
                return true;
            }
            
            // Set the position
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
            
            // Save mines
            mineManager.saveMines();
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            } else if (args.length == 3) {
                return Arrays.asList("1", "2");
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for resetting a mine
     */
    private class ResetSubCommand extends SubCommand {
        
        public ResetSubCommand() {
            super("reset", "convergence.mine.reset", false);
            setAliases("r");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine reset <type> <id>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Check if mine has a valid region
            if (!mine.hasValidRegion()) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.no-region", 
                        Map.of("name", mine.getName()));
                return true;
            }
            
            // Reset the mine
            mineManager.resetMine(mine);
            
            // Send success message
            plugin.getMessageService().sendMessage(sender, "prison.mine.reset", 
                    Map.of("name", mine.getName()));
            
            return true;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            }
            
            return new ArrayList<>();
        }
    }
    
    /**
     * Subcommand for setting mine composition
     */
    private class CompositionSubCommand extends SubCommand {
        
        public CompositionSubCommand() {
            super("composition", "convergence.mine.composition", false);
            setAliases("comp");
        }
        
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 4) {
                plugin.getMessageService().sendMessage(sender, "general.invalid-command", 
                        Map.of("command", "mine composition <type> <id> <material> <percentage>"));
                return true;
            }
            
            // Parse mine type
            MineType type;
            try {
                type = MineType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-type", 
                        Map.of("type", args[0]));
                return true;
            }
            
            String id = args[1];
            
            // Get the mine
            Mine mine = mineManager.getMine(id, type);
            if (mine == null) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-mine", 
                        Map.of("name", id));
                return true;
            }
            
            // Parse material
            Material material;
            try {
                material = Material.valueOf(args[2].toUpperCase());
                if (!material.isBlock()) {
                    plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-block", 
                            Map.of("material", args[2]));
                    return true;
                }
            } catch (IllegalArgumentException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-material", 
                        Map.of("material", args[2]));
                return true;
            }
            
            // Parse percentage
            double percentage;
            try {
                percentage = Double.parseDouble(args[3]);
                if (percentage < 0 || percentage > 100) {
                    plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-percentage", 
                            Map.of("percentage", args[3]));
                    return true;
                }
            } catch (NumberFormatException e) {
                plugin.getMessageService().sendMessage(sender, "prison.mine.invalid-percentage", 
                        Map.of("percentage", args[3]));
                return true;
            }
            
            // Update mine composition
            mine.addMaterial(material, percentage);
            
            // Normalize percentages if needed
            double total = mine.getComposition().values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(total - 100.0) > 0.01) {
                // Warn if total is not close to 100%
                sender.sendMessage(plugin.getMessageService().colorize("&eWarning: Total composition is " + 
                        String.format("%.2f", total) + "%. Consider adjusting to total 100%."));
            }
            
            // Save mines
            mineManager.saveMines();
            
            // Send success message
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
                return Arrays.stream(MineType.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                try {
                    MineType type = MineType.valueOf(args[0].toUpperCase());
                    return mineManager.getMinesByType(type).stream()
                            .map(Mine::getId)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return new ArrayList<>();
                }
            } else if (args.length == 3) {
                // Return a list of block materials
                return Arrays.stream(Material.values())
                        .filter(Material::isBlock)
                        .filter(m -> !m.name().contains("LEGACY"))
                        .map(Material::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            } else if (args.length == 4) {
                // Suggest some common percentages
                return Arrays.asList("5", "10", "15", "20", "25", "30", "50", "75", "100");
            }
            
            return new ArrayList<>();
        }
    }
}