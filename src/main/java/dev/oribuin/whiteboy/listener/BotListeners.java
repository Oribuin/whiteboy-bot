package dev.oribuin.whiteboy.listener;

import dev.oribuin.whiteboy.WhiteBoyBot;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotListeners extends ListenerAdapter {

    private final WhiteBoyBot bot;

    public BotListeners(WhiteBoyBot bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        this.bot.getDataManager().loadServer(event.getGuild());
        System.out.println(" - Loaded Server: " + event.getGuild().getName());
    }

}
