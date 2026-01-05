package com.aethor.aethorquests.ui;

import com.aethor.aethorquests.model.dialogue.DialogueNode;
import com.aethor.aethorquests.model.dialogue.DialogueOption;
import com.aethor.aethorquests.model.dialogue.DialogueSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles rendering of dialogue to players using Adventure components.
 * Provides consistent, immersive formatting for quest dialogues.
 */
public class DialogueRenderer {
    
    private final FileConfiguration config;
    private final MiniMessage miniMessage;
    
    // Configuration keys
    private static final String CONFIG_PREFIX_COLOR = "dialogue.prefix-color";
    private static final String CONFIG_NPC_COLOR = "dialogue.npc-color";
    private static final String CONFIG_TEXT_COLOR = "dialogue.text-color";
    private static final String CONFIG_HINT_LOCATION = "dialogue.hint-location"; // "chat" or "actionbar"
    private static final String CONFIG_HINT_TEXT = "dialogue.hint-text";
    
    // Default values
    private static final NamedTextColor DEFAULT_PREFIX_COLOR = NamedTextColor.GOLD;
    private static final NamedTextColor DEFAULT_NPC_COLOR = NamedTextColor.YELLOW;
    private static final NamedTextColor DEFAULT_TEXT_COLOR = NamedTextColor.WHITE;
    private static final String DEFAULT_HINT = "(Press [SHIFT] to continue)";
    
    public DialogueRenderer(FileConfiguration config) {
        this.config = config;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    /**
     * Renders and sends the current dialogue line to the player.
     *
     * @param player The player to send the dialogue to
     * @param session The active dialogue session
     */
    public void renderCurrentLine(Player player, DialogueSession session) {
        String line = session.getCurrentLine();
        if (line == null) {
            return;
        }
        
        // Format with quote marks for a more polished look
        Component message = Component.text("  ", NamedTextColor.DARK_GRAY)
                .append(Component.text("❝ ", NamedTextColor.GRAY))
                .append(parseMiniMessage(session.getNpcName()))
                .append(Component.text(" › ", NamedTextColor.DARK_GRAY))
                .append(parseDialogueText(line));
        
        player.sendMessage(message);
        
        // Show continuation hint if there are more lines
        if (!session.isWaitingForChoice() && session.getCurrentLineIndex() < session.getCurrentNode().getLines().size() - 1) {
            showContinueHint(player);
        }
    }
    
    /**
     * Renders dialogue choices to the player.
     *
     * @param player The player to send the choices to
     * @param session The active dialogue session
     */
    public void renderChoices(Player player, DialogueSession session) {
        DialogueNode currentNode = session.getCurrentNode();
        if (currentNode == null || !currentNode.hasOptions()) {
            return;
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ╭───────────────────────────╮", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  │ ", NamedTextColor.DARK_GRAY)
                .append(Component.text("Available Responses", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("       │", NamedTextColor.DARK_GRAY)));
        player.sendMessage(Component.text("  ╰───────────────────────────╯", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
        
        for (DialogueOption option : currentNode.getOptions()) {
            // Icon based on common option patterns
            String icon = getOptionIcon(option.getText());
            NamedTextColor optionColor = getOptionColor(option.getText());
            
            Component optionLine = Component.text("    " + icon + " ", optionColor, TextDecoration.BOLD)
                    .append(Component.text("[" + option.getId() + "] ", NamedTextColor.GRAY))
                    .append(Component.text(translateColorCodes(option.getText()), optionColor))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("━━━━━━━━━━━━━━━━", NamedTextColor.GRAY)
                                    .append(Component.newline())
                                    .append(Component.text("➥ ", optionColor))
                                    .append(Component.text("Click to select", NamedTextColor.WHITE))
                                    .append(Component.newline())
                                    .append(Component.text("━━━━━━━━━━━━━━━━", NamedTextColor.GRAY))
                    ))
                    .clickEvent(ClickEvent.runCommand("/quest dialogue select " + option.getId()));
            
            player.sendMessage(optionLine);
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ⌨ ", NamedTextColor.DARK_GRAY)
                .append(Component.text("Press ", NamedTextColor.GRAY))
                .append(Component.text("[1-9]", NamedTextColor.YELLOW))
                .append(Component.text(" or ", NamedTextColor.GRAY))
                .append(Component.text("Click", NamedTextColor.AQUA))
                .append(Component.text(" to choose", NamedTextColor.GRAY)));
        player.sendMessage(Component.empty());
    }
    
    /**
     * Shows the continuation hint to the player.
     *
     * @param player The player to show the hint to
     */
    private void showContinueHint(Player player) {
        String hintLocation = config.getString(CONFIG_HINT_LOCATION, "actionbar");
        String hintText = config.getString(CONFIG_HINT_TEXT, DEFAULT_HINT);
        
        Component hint = Component.text("  ⇩ ", NamedTextColor.DARK_GRAY)
                .append(Component.text(translateColorCodes(hintText), NamedTextColor.GRAY, TextDecoration.ITALIC));
        
        if ("actionbar".equalsIgnoreCase(hintLocation)) {
            player.sendActionBar(hint);
        } else {
            player.sendMessage(hint);
        }
    }
    
    /**
     * Renders a dialogue start notification.
     *
     * @param player The player
     * @param npcName Name of the NPC
     */
    public void renderDialogueStart(Player player, String npcName) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ╔═══════════════════════════════════════╗", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  ║ ", NamedTextColor.GOLD)
                .append(Component.text("⚐ ", NamedTextColor.YELLOW))
                .append(Component.text("Conversation with ", NamedTextColor.GRAY))
                .append(parseMiniMessage(npcName))
                .append(Component.text("        ║", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("  ╚═══════════════════════════════════════╝", NamedTextColor.GOLD));
        player.sendMessage(Component.empty());
    }
    
    /**
     * Renders a dialogue end notification.
     *
     * @param player The player
     */
    public void renderDialogueEnd(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ╚═══════════════════════════════════════╝", NamedTextColor.GRAY));
        player.sendMessage(Component.text("    Conversation ended", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
        player.sendMessage(Component.empty());
    }
    
    /**
     * Parses MiniMessage format into Adventure Component.
     * Falls back to plain text with default color if parsing fails.
     *
     * @param text The text to parse (may contain MiniMessage tags)
     * @return Parsed Component
     */
    private Component parseMiniMessage(String text) {
        try {
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            // Fallback to plain text if parsing fails
            return Component.text(text, getNpcColor());
        }
    }
    
    /**
     * Parses dialogue text with MiniMessage support and color codes.
     *
     * @param text The dialogue text
     * @return Parsed Component
     */
    private Component parseDialogueText(String text) {
        try {
            // Try MiniMessage first
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            // Fallback to legacy color codes
            return Component.text(translateColorCodes(text), getTextColor());
        }
    }
    
    /**
     * Gets an icon based on option text content.
     *
     * @param optionText The option text
     * @return Icon character
     */
    private String getOptionIcon(String optionText) {
        String lower = optionText.toLowerCase();
        if (lower.contains("accept") || lower.contains("yes") || lower.contains("agree") || lower.contains("sure")) {
            return "✓";
        } else if (lower.contains("decline") || lower.contains("no") || lower.contains("reject") || lower.contains("refuse")) {
            return "✗";
        } else if (lower.contains("turn in") || lower.contains("complete") || lower.contains("finish")) {
            return "✔";
        } else if (lower.contains("ask") || lower.contains("question") || lower.contains("why")) {
            return "?";
        } else if (lower.contains("leave") || lower.contains("goodbye") || lower.contains("exit")) {
            return "↩";
        } else {
            return "►";
        }
    }
    
    /**
     * Gets a color based on option text content.
     *
     * @param optionText The option text
     * @return Color for the option
     */
    private NamedTextColor getOptionColor(String optionText) {
        String lower = optionText.toLowerCase();
        if (lower.contains("accept") || lower.contains("yes") || lower.contains("agree") || lower.contains("turn in")) {
            return NamedTextColor.GREEN;
        } else if (lower.contains("decline") || lower.contains("no") || lower.contains("reject")) {
            return NamedTextColor.RED;
        } else if (lower.contains("ask") || lower.contains("question")) {
            return NamedTextColor.AQUA;
        } else if (lower.contains("leave") || lower.contains("goodbye")) {
            return NamedTextColor.GRAY;
        } else {
            return NamedTextColor.YELLOW;
        }
    }
    
    /**
     * Translates legacy color codes (&) to section signs (§).
     *
     * @param text The text to translate
     * @return The translated text
     */
    private String translateColorCodes(String text) {
        return text.replace("&", "§");
    }
    
    // Configuration getters with defaults
    
    private NamedTextColor getPrefixColor() {
        String colorName = config.getString(CONFIG_PREFIX_COLOR, DEFAULT_PREFIX_COLOR.toString());
        return parseColor(colorName, DEFAULT_PREFIX_COLOR);
    }
    
    private NamedTextColor getNpcColor() {
        String colorName = config.getString(CONFIG_NPC_COLOR, DEFAULT_NPC_COLOR.toString());
        return parseColor(colorName, DEFAULT_NPC_COLOR);
    }
    
    private NamedTextColor getTextColor() {
        String colorName = config.getString(CONFIG_TEXT_COLOR, DEFAULT_TEXT_COLOR.toString());
        return parseColor(colorName, DEFAULT_TEXT_COLOR);
    }
    
    private NamedTextColor parseColor(String colorName, NamedTextColor defaultColor) {
        try {
            return NamedTextColor.NAMES.value(colorName.toLowerCase());
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
