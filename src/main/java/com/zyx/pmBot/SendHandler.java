package com.zyx.pmBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class SendHandler extends ListenerAdapter {
    private final BotConfig cfg;

    public SendHandler(BotConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("send")) return;

        Member member = event.getMember();
        if (!Security.canUse(member, cfg)) {
            event.reply("No permission.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String sub = event.getSubcommandName();
        OptionMapping chOpt = event.getOption("channel");
        if (chOpt == null || !(chOpt.getAsChannel() instanceof TextChannel target)) {
            event.reply("Choose a channel.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        switch (sub) {
            case "message_only" -> event.replyModal(buildMessageOnlyModal(target)).queue();
            case "embed_only" -> event.replyModal(buildEmbedOnlyModal(target)).queue();
            case "message_and_embed" -> event.replyModal(buildMessageAndEmbedModal(target)).queue();
            default -> event.reply("Unknown subcommand.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("send:")) return;

        String[] parts = event.getModalId().split(":", 3); // send:type:channelId
        if (parts.length < 3) return;

        String type = parts[1];
        String channelId = parts[2];
        TextChannel channel = event.getJDA().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Channel not found.").setEphemeral(true).queue();
            return;
        }
        if (!Security.canUse(event.getMember(), cfg)) {
            event.reply("No permission.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        switch (type) {
            case "msg" -> handleMessageOnly(event, channel);
            case "emb" -> handleEmbedOnly(event, channel);
            case "both" -> handleBoth(event, channel);
        }
    }

    private Modal buildMessageOnlyModal(TextChannel channel) {
        TextInput message = TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(4000)
                .build();

        return Modal.create("send:msg:" + channel.getId(), "Send message → " + channel.getName())
                .addActionRow(message)
                .build();
    }

    private Modal buildEmbedOnlyModal(TextChannel channel) {
        TextInput title = TextInput.create("title", "Embed-Titel (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(256)
                .build();
        TextInput desc = TextInput.create("description", "Embed-Description", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setMaxLength(4000)
                .build();
        TextInput color = TextInput.create("color", "Hex-Color (z. B. #ff66cc, optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(16)
                .build();
        TextInput footer = TextInput.create("footer", "Footer (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(2048)
                .build();
        TextInput image = TextInput.create("image", "Image-URL (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(1024)
                .build();

        return Modal.create("send:emb:" + channel.getId(), "Send embed → " + channel.getName())
                .addActionRow(title)
                .addActionRow(desc)
                .addActionRow(color)
                .addActionRow(footer)
                .addActionRow(image)
                .build();
    }

    private Modal buildMessageAndEmbedModal(TextChannel channel) {
        TextInput message = TextInput.create("message", "Message (optional)", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .setMaxLength(4000)
                .build();
        TextInput title = TextInput.create("title", "Embed-Titel (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(256)
                .build();
        TextInput desc = TextInput.create("description", "Embed-Description (optional)", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .setMaxLength(4000)
                .build();
        TextInput color = TextInput.create("color", "Hex-Color (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(16)
                .build();
        TextInput image = TextInput.create("image", "Image-URL (optional)", TextInputStyle.SHORT)
                .setRequired(false)
                .setMaxLength(1024)
                .build();

        return Modal.create("send:both:" + channel.getId(), "Send message + embed → " + channel.getName())
                .addActionRow(message)
                .addActionRow(title)
                .addActionRow(desc)
                .addActionRow(color)
                .addActionRow(image)
                .build();
    }

    private void handleMessageOnly(ModalInteractionEvent event, TextChannel channel) {
        String msg = get(event, "message");
        if (msg.isBlank()) {
            event.reply("Message is empty.").setEphemeral(true).queue();
            return;
        }
        channel.sendMessage(msg).queue(
                ok -> event.reply("Sent message in <#" + channel.getId() + ">.")
                        .setEphemeral(true).queue(),
                err -> event.reply("Message send error: " + err.getMessage())
                        .setEphemeral(true).queue()
        );
    }

    private void handleEmbedOnly(ModalInteractionEvent event, TextChannel channel) {
        EmbedBuilder eb = buildEmbedFromModal(event);
        if (eb == null) {
            event.reply("Embed description is needed.")
                    .setEphemeral(true).queue();
            return;
        }
        channel.sendMessageEmbeds(eb.build()).queue(
                ok -> event.reply("Embed sent in <#" + channel.getId() + ">.")
                        .setEphemeral(true).queue(),
                err -> event.reply("Embed send error: " + err.getMessage())
                        .setEphemeral(true).queue()
        );
    }

    private void handleBoth(ModalInteractionEvent event, TextChannel channel) {
        String message = get(event, "message");
        EmbedBuilder eb = buildEmbedFromModal(event);
        if (message.isBlank() && eb == null) {
            event.reply("Message or Embed description needed.")
                    .setEphemeral(true).queue();
            return;
        }

        if (eb == null) {
            channel.sendMessage(message).queue(
                    ok -> event.reply("Sent.").setEphemeral(true).queue(),
                    err -> event.reply("Error: " + err.getMessage()).setEphemeral(true).queue()
            );
        } else {
            channel.sendMessage(message.isBlank() ? " " : message)
                    .setEmbeds(eb.build())
                    .queue(
                            ok -> event.reply("Message + Embed sent.").setEphemeral(true).queue(),
                            err -> event.reply("Error: " + err.getMessage()).setEphemeral(true).queue()
                    );
        }
    }

    private EmbedBuilder buildEmbedFromModal(ModalInteractionEvent event) {
        String desc = get(event, "description");
        if (desc.isBlank()) return null;

        String title = get(event, "title");
        String colorHex = get(event, "color");
        String footer = get(event, "footer");
        String image = get(event, "image");

        EmbedBuilder eb = new EmbedBuilder();
        if (!title.isBlank()) eb.setTitle(title);
        eb.setDescription(desc);
        if (!colorHex.isBlank()) {
            try {
                eb.setColor(Color.decode(colorHex));
            } catch (Exception ignored) {
            }
        }
        if (!footer.isBlank()) eb.setFooter(footer);
        if (!image.isBlank()) eb.setImage(image);
        eb.setTimestamp(Instant.now());
        return eb;
    }

    private String get(ModalInteractionEvent e, String id) {
        ModalMapping m = e.getValue(id);
        return m == null ? "" : m.getAsString().trim();
    }
}

