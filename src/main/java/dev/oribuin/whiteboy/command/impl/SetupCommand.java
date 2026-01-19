package dev.oribuin.whiteboy.command.impl;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.command.BotCommand;
import dev.oribuin.whiteboy.manager.DataManager;
import dev.oribuin.whiteboy.model.InspiredServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.discord.jda5.annotation.ReplySetting;

import java.awt.*;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public class SetupCommand implements BotCommand {

    @Command("setupwhiteboy channel <channel>")
    @ReplySetting(defer = true)
    public void setupChannel(CommandContext<JDAInteraction> context, Channel channel) {
        JDAInteraction sender = context.sender();
        InteractionHook hook = sender.interactionEvent().getHook();

        Guild guild = sender.guild();
        if (guild == null) return;

        Member member = guild.getMemberById(sender.user().getId());
        if (member == null) return;

        if (!(channel instanceof TextChannel)) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("You need to provide a text channel")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        // only server owner can run command because im lazy
        if (!member.isOwner()) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Only the server owner can run this command")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        DataManager dataManager = WhiteBoyBot.getInstance().getDataManager();
        InspiredServer server = dataManager.getServer(guild);
        server.setChannel(channel.getIdLong());
        dataManager.inspireServer(server);

        hook.sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode("#94bc80"))
                .setAuthor("white boy up!")
                .setDescription("Changed the inspiration channel to " + channel.getAsMention())
                .build()
        ).setEphemeral(true).queue();
    }

    @Command("setupwhiteboy timezone <timezone>")
    @ReplySetting(defer = true)
    public void setupZone(
            CommandContext<JDAInteraction> context, 
            @Argument(value = "timezone", suggestions = "timezones") String timezone
    ) {
        JDAInteraction sender = context.sender();
        InteractionHook hook = sender.interactionEvent().getHook();

        Guild guild = sender.guild();
        if (guild == null) return;

        Member member = guild.getMemberById(sender.user().getId());
        if (member == null) return;


        // only server owner can run command because im lazy
        if (!member.isOwner()) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Only the server owner can run this command")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        if (timeZone == null) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Couldn't find your timezone? You can view the list of timezone ids here https://en.wikipedia.org/wiki/List_of_tz_database_time_zones#List")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }
        
        DataManager dataManager = WhiteBoyBot.getInstance().getDataManager();
        InspiredServer server = dataManager.getServer(guild);
        server.setTimeZone(timeZone);
        dataManager.inspireServer(server);

        hook.sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode("#94bc80"))
                .setAuthor("white boy up!")
                .setDescription("Changed the timezone to **" + server.getTimeZone().getDisplayName() + "** [" + server.getTimeZone().toZoneId() + "]\n\nAll Time Zone Ids are available Here: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones#List")
                .build()
        ).setEphemeral(true).queue();
    }

    @Command("setupwhiteboy role <role>")
    @ReplySetting(defer = true)
    public void setupRole(CommandContext<JDAInteraction> context, Role role) {
        JDAInteraction sender = context.sender();
        InteractionHook hook = sender.interactionEvent().getHook();

        Guild guild = sender.guild();
        if (guild == null) return;

        Member member = guild.getMemberById(sender.user().getId());
        if (member == null) return;

        // only server owner can run command because im lazy
        if (!member.isOwner()) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Only the server owner can run this command")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        DataManager dataManager = WhiteBoyBot.getInstance().getDataManager();
        InspiredServer server = dataManager.getServer(guild);
        server.setRole(role.getIdLong());
        dataManager.inspireServer(server);

        hook.sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode("#94bc80"))
                .setAuthor("white boy up!")
                .setDescription("Changed the inspiration role to " + role.getAsMention())
                .build()
        ).setEphemeral(true).queue();
    }

    @Suggestions("timezones")
    public List<String> suggest(CommandContext<JDAInteraction> context, String input) {
        return DEFAULT_TIMEZONES.stream().sorted().toList();
    }

    private static final java.util.List<String> DEFAULT_TIMEZONES = List.of(
            "what?",
            "America/Los_Angeles",
            "America/Phoenix",
            "America/Texas",
            "America/New_York",
            "America/Chicago",
            "Asia/Tokyo",
            "Australia/Sydney",
            "Europe/London",
            "Europe/Paris",
            "Europe/Dublin",
            "Pacific/Honolulu",
            "Europe/Kyiv",
            "Europe/Madrid,",
            "Europe/Rome",
            "Europe/Berlin",
            "Africa/Cairo",
            "Asia/Bangkok",
            "Europe/Istanbul"
    );

}
