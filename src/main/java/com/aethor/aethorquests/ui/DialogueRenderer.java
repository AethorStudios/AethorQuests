package com.aethor.aethorquests.ui;

import com.aethor.aethorquests.model.dialogue.DialogueNode;
import com.aethor.aethorquests.model.dialogue.DialogueOption;
import com.aethor.aethorquests.model.dialogue.DialogueSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles rendering of dialogue to players using Adventure components.
 * Provides consistent, immersive formatting for quest dialogues.
 */
public class DialogueRenderer {
    
    private final FileConfiguration config;
    
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
        
        // Format: [Quest] <NPC Name>: <Dialogue Text>
        Component message = Component.empty()
                .append(Component.text("[Quest] ", getPrefixColor(), TextDecoration.BOLD))
                .append(Component.text(session.getNpcName(), getNpcColor()))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(translateColorCodes(line), getTextColor()));
        
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
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Choose a response:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        player.sendMessage(Component.empty());
        
        for (DialogueOption option : currentNode.getOptions()) {
            Component optionLine = Component.text(option.getId() + ") ", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(translateColorCodes(option.getText()), NamedTextColor.WHITE))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to select")))
                    .clickEvent(ClickEvent.runCommand("/quest dialogue select " + option.getId()));
            
            player.sendMessage(optionLine);
        }
        
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("(Click an option or press number key 1-9)", NamedTextColor.GRAY));
    }
    
    /**
     * Shows the continuation hint to the player.
     *
     * @param player The player to show the hint to
     */
    private void showContinueHint(Player player) {
        String hintLocation = config.getString(CONFIG_HINT_LOCATION, "actionbar");
        String hintText = config.getString(CONFIG_HINT_TEXT, DEFAULT_HINT);
        
        Component hint = Component.text(translateColorCodes(hintText), NamedTextColor.GRAY);
        
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
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("Talking with ", NamedTextColor.GRAY)
                .append(Component.text(npcName, getNpcColor(), TextDecoration.BOLD)));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
    }
    
    /**
     * Renders a dialogue end notification.
     *
     * @param player The player
     */
    public void renderDialogueEnd(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
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
