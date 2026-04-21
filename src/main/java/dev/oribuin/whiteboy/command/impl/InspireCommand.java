package dev.oribuin.whiteboy.command.impl;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.command.BotCommand;
import dev.oribuin.whiteboy.manager.DataManager;
import dev.oribuin.whiteboy.model.InspiredServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.jda6.annotation.ReplySetting;

import java.awt.*;

public class InspireCommand implements BotCommand {

    @Command("inspire")
    @ReplySetting(defer = true)
    public void inspire(CommandContext<JDAInteraction> context) {
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
        server.inspire();

        hook.sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.decode("#94bc80"))
                .setAuthor("white boy up!")
                .setDescription("Your daily dose of white boy inspiration is being shipped.")
                .build()
        ).setEphemeral(true).queue();
    }

}
