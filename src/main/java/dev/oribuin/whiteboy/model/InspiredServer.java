package dev.oribuin.whiteboy.model;

import dev.oribuin.whiteboy.WhiteBoyBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.TimeZone;

public class InspiredServer {

    private final long id;
    private TimeZone timeZone;
    private String inspiration;
    private Long role;
    private Long channel;
    private long lastInspiration;

    public InspiredServer(long id, TimeZone timeZone, String inspiration, Long role, Long channel, long lastInspiration) {
        this.id = id;
        this.timeZone = timeZone;
        this.inspiration = inspiration;
        this.role = role;
        this.channel = channel;
        this.lastInspiration = lastInspiration;
    }

    public InspiredServer(long id, TimeZone timeZone, String inspiration, Long role, Long channel) {
        this.id = id;
        this.timeZone = timeZone;
        this.inspiration = inspiration;
        this.role = role;
        this.channel = channel;
        this.lastInspiration = 0L;
    }

    public InspiredServer(long id, TimeZone timeZone, Long role, Long channel) {
        this.id = id;
        this.timeZone = timeZone;
        this.inspiration = null;
        this.role = role;
        this.channel = channel;
        this.lastInspiration = 0L;
    }

    /**
     * inspire the masses... white boy
     */
    public void inspire() {
        this.lastInspiration = System.currentTimeMillis(); // reset last inspiration even if it fails to stop it from trying constantly

        Guild guild = WhiteBoyBot.getInstance().getApplication().getGuildById(id);
        if (guild == null || this.channel == null) return;

        TextChannel textChannel = guild.getTextChannelById(this.channel);
        if (textChannel == null) return; // server doesn't have a channel reset and/or it doesnt exist

        // Select the white boy inspiration of the day smile
        File file = WhiteBoyBot.getInstance().getWhiteBoyManager().generateInspiration();
        if (file == null) return; // couldn't find an inspiration, maybe needs error logging

        try (FileUpload upload = FileUpload.fromData(file)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("listen up... white boy")
                    .setImage("attachment://" + upload.getName())
                    .build();

            MessageCreateAction action = textChannel.sendMessageEmbeds(embed).addFiles(upload);
            if (this.role != null) {
                Role serverRole = guild.getRoleById(this.role);
                if (serverRole != null) action.addContent(serverRole.getAsMention());
            }

            action.queue();
        } catch (IOException ex) {
            System.out.println("* Server Error [" + guild.getName() + "]: " + ex.getMessage());
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("(i couldn't upload an inspiration. contact oribuin)")
                    .build();

            textChannel.sendMessageEmbeds(embed).queue();
        }

        WhiteBoyBot.getInstance().getDataManager().update(this);
    }

    /**
     * check if the server can be inspired... white boy
     *
     * @return If there has been an inspiration within the past hour
     */
    public boolean canInspire() {
        if (channel == null) return false; // no channel being set means white boy can't inspire

        // don't inspire twice within an hour or whatever... white boy
        boolean result = System.currentTimeMillis() - this.lastInspiration > Duration.ofHours(1).toMillis();
        if (!result) System.out.println("* Info - Could not inspire because it has already been done within the past hour");
        return result;
    }

    /**
     * load the server... white boy
     *
     * @param set The result set being loaded
     * @return The resulting server
     * @throws SQLException Any white boy exceptions that may occur
     */
    public static InspiredServer from(ResultSet set) throws SQLException {
        long id = set.getLong("serverId");
        TimeZone timezone = TimeZone.getTimeZone(set.getString("timezone"));
        String inspiration = set.getString("inspiration");
        Long role = set.getLong("role");
        Long channel = set.getLong("channel");
        long lastInspiration = set.getLong("lastInspiration");
        return new InspiredServer(id, timezone, inspiration, role, channel, lastInspiration);
    }

    public long getId() {
        return id;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getInspiration() {
        return inspiration;
    }

    public void setInspiration(String inspiration) {
        this.inspiration = inspiration;
    }

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }

    public Long getChannel() {
        return channel;
    }

    public void setChannel(Long channel) {
        this.channel = channel;
    }

    public long getLastInspiration() {
        return lastInspiration;
    }

    public void setLastInspiration(long lastInspiration) {
        this.lastInspiration = lastInspiration;
    }

}
