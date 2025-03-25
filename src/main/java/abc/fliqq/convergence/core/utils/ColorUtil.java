package abc.fliqq.convergence.core.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    
    /**
     * Colorizes a string with color codes and hex colors
     * 
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String colorize(String text) {
        if (text == null) return "";
        
        // Replace hex colors
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        
        matcher.appendTail(buffer);
        
        // Replace standard color codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    /**
     * Strips all color codes from a string
     * 
     * @param text The text to strip
     * @return The stripped text
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }
    
    /**
     * Generates a gradient between two colors
     * 
     * @param text The text to apply the gradient to
     * @param startColor The starting hex color
     * @param endColor The ending hex color
     * @return The text with gradient applied
     */
    public static String gradient(String text, String startColor, String endColor) {
        if (text == null || text.isEmpty()) return "";
        
        // Parse hex colors
        java.awt.Color start = java.awt.Color.decode(startColor);
        java.awt.Color end = java.awt.Color.decode(endColor);
        
        // Calculate step size for each component
        float stepR = (end.getRed() - start.getRed()) / (float) (text.length() - 1);
        float stepG = (end.getGreen() - start.getGreen()) / (float) (text.length() - 1);
        float stepB = (end.getBlue() - start.getBlue()) / (float) (text.length() - 1);
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            // Calculate current color
            int r = Math.round(start.getRed() + (stepR * i));
            int g = Math.round(start.getGreen() + (stepG * i));
            int b = Math.round(start.getBlue() + (stepB * i));
            
            // Ensure values are within valid range
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            
            // Convert to hex and append character
            String hex = String.format("#%02x%02x%02x", r, g, b);
            result.append(ChatColor.of(hex)).append(text.charAt(i));
        }
        
        return result.toString();
    }
}