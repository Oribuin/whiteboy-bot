package dev.oribuin.whiteboy.util;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public final class DefaultEmbeds {

    public static EmbedBuilder getErrorEmbed(String reason, String message) {
        return new EmbedBuilder()
                .setColor(Color.decode("#fa5d48"))
                .setAuthor("\uD83D\uDC94 Error Occurred")
                .setDescription("[**" + reason + "**] " + message);
    }

}
