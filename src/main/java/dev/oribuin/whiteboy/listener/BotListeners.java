package dev.oribuin.whiteboy.listener;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.model.Submission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static dev.oribuin.whiteboy.manager.WhiteBoyManager.INSPIRATION_FOLDER;

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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Submission submission = this.bot.getDataManager().getSubmissions().get(event.getMessageIdLong());
        if (submission == null) return;

        Button button = event.getButton();
        String buttonId = button.getCustomId();
        if (buttonId == null) return;

        // Button.of(ButtonStyle.SUCCESS, "whiteboy-accept", "Accept"),
        // Button.of(ButtonStyle.DANGER, "whiteboy-deny", "Deny")
        switch (buttonId) {
            case "whiteboy-accept" -> {
                event.getMessage().editMessageComponents(Collections.emptyList()).queue();
                try {
                    if (submission.transfer(INSPIRATION_FOLDER)) {
                        this.bot.getWhiteBoyManager().loadInspirations();
                        this.bot.getDataManager().deleteSubmission(submission);
                        event.replyEmbeds(new EmbedBuilder()
                                .setColor(Color.decode("#94bc80"))
                                .setAuthor("Submission Accepted")
                                .setDescription("You have accepted the submission to be rotated into the bot posts")
                                .build()
                        ).setEphemeral(true).queue();
                        return;
                    }
                } catch (IOException ex) {
                    System.out.println("Ran into issue accepting a submission: " + ex.getMessage());
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setColor(Color.decode("#94bc80"))
                        .setAuthor("white boy down....")
                        .setDescription("The bot could not successfully accept the request")
                        .build()
                ).setEphemeral(true).queue();
            }

            case "whiteboy-deny" -> {
                event.getMessage().editMessageComponents(Collections.emptyList()).queue();
                File file = submission.getFile();
                file.delete();
                this.bot.getDataManager().deleteSubmission(submission);

                event.replyEmbeds(new EmbedBuilder()
                        .setColor(Color.decode("#94bc80"))
                        .setAuthor("Submission Denied")
                        .setDescription("You have denied the submission from " + submission.getUsername())
                        .build()
                ).setEphemeral(true).queue();
            }
        }
    }
}
