package dev.oribuin.whiteboy.command.impl;

import dev.oribuin.whiteboy.WhiteBoyBot;
import dev.oribuin.whiteboy.command.BotCommand;
import dev.oribuin.whiteboy.model.ServerIds;
import dev.oribuin.whiteboy.model.Submission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.jda6.annotation.ReplySetting;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class SubmissionCommand implements BotCommand {

    private static final Random RANDOM = new Random();

    @Command("whitebot submit <attachment>")
    @ReplySetting(defer = true)
    public void inspire(CommandContext<JDAInteraction> context, Message.Attachment attachment) {
        JDAInteraction sender = context.sender();
        InteractionHook hook = sender.interactionEvent().getHook();

        // TODO: Check if the user is banned from submissions
        // Check if attachment is an image 
        if (!attachment.isImage()) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Sorry! You can only use `.png` or `.jpg` files as submissions")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        // Check if attachment is a spoiler image
        if (attachment.isSpoiler()) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Sorry! Images that are marked as a spoiler cannot be used")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }
        // Access oribuin to make sure we can send it
        User botOwner = WhiteBoyBot.getInstance().getApplication().getUserById(ServerIds.ORIBUIN_ID);
        if (botOwner == null) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("We couldn't access <@" + ServerIds.ORIBUIN_ID + ">'s User for submissions")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        // Download the image into submissions
        File baseFolder = new File("submissions");
        if (!baseFolder.exists()) baseFolder.mkdir();

        String fileName = attachment.getId() + "." + attachment.getFileExtension();
        File target = new File(baseFolder, fileName);
        try {
            InputStream in = new URL(attachment.getUrl()).openStream();
            Files.copy(in, Paths.get(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("white boy down...")
                    .setDescription("Failed to download the submission, Please contact `oribuin` about this issue\n\n" + ex.getMessage())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        Submission submission = new Submission(
                sender.user().getIdLong(),
                sender.user().getName(),
                baseFolder.getName() + "/" + target.getName(),
                System.currentTimeMillis()
        );

        botOwner.openPrivateChannel().queue(channel -> {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("New White Boy Submission")
                    .setDescription(String.format(
                            "User %s (%s) has requested a new addition for the white boy bot",
                            sender.user().getAsMention(),
                            sender.user().getName())
                    )
                    .setImage(attachment.getUrl());

            Guild guild = sender.guild();
            if (guild != null) builder.setFooter("This was submitted from guild " + guild.getName());
            else builder.setFooter("This was submitted directly in messages");

            hook.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.decode("#94bc80"))
                    .setAuthor("Submission Sent")
                    .setDescription("We have sent your image submission for review to make sure it's acceptable")
                    .build()
            ).setEphemeral(true).queue();
            
            channel.sendMessageEmbeds(builder.build())
                    .setComponents(ActionRow.of(
                            Button.of(ButtonStyle.SUCCESS, "whiteboy-accept", "Accept"),
                            Button.of(ButtonStyle.DANGER, "whiteboy-deny", "Deny")
                    )).queue(message -> {
                        submission.setMessage(message.getIdLong());
                        WhiteBoyBot.getInstance().getDataManager().saveSubmission(submission);
                    });
        });
    }

}
    