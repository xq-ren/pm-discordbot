package com.zyx.pmBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;


import java.time.Instant;


public class AdPostHandler extends ListenerAdapter {
    private final BotConfig cfg;
    public AdPostHandler(BotConfig cfg) { this.cfg = cfg; }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("adpost")) return;
        Member member = event.getMember();
        if (!Security.canUse(member, cfg)) {
            event.reply("You don't have permission to use this.").setEphemeral(true).queue();
            return;
        }
        OptionMapping chOpt = event.getOption("channel");
        if (chOpt == null || !(chOpt.getAsChannel() instanceof TextChannel target)) {
            event.reply("Please pick a text channel.").setEphemeral(true).queue();
            return;
        }

        String customId = "adpost:" + target.getId();


        TextInput normal = TextInput.create("normal", "Normal ad (plain text)", TextInputStyle.PARAGRAPH)
                .setMaxLength(4000).setRequired(true).build();
        TextInput code = TextInput.create("code", "Codeblock ad (will be wrapped in ```)", TextInputStyle.PARAGRAPH)
                .setMaxLength(4000).setRequired(true).build();


        Modal modal = Modal.create(customId, "Post Ad → " + target.getName())
                .addActionRow(normal)
                .addActionRow(code)
                .build();
        event.replyModal(modal).queue();
    }


    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("adpost:")) return;
        String channelId = event.getModalId().substring("adpost:".length());
        TextChannel channel = event.getJDA().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Target channel not found.").setEphemeral(true).queue();
            return;
        }
        if (!Security.canUse(event.getMember(), cfg)) {
            event.reply("You don't have permission to use this.").setEphemeral(true).queue();
            return;
        }
        ModalMapping normalMap = event.getValue("normal");
        ModalMapping codeMap = event.getValue("code");
        String normal = normalMap == null ? "" : normalMap.getAsString();
        String code = codeMap == null ? "" : codeMap.getAsString();


        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Codeblock Ad");
        eb.setDescription("```\n" + safeTrimForCodeblock(code) + "\n```");
        eb.setTimestamp(Instant.now());
        eb.setFooter("Posted by " + event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl());


        channel.sendMessage(normal)
                .setEmbeds(eb.build())
                .queue(
                        ok -> event.reply("Ad posted in <#" + channel.getId() + ">").setEphemeral(true).queue(),
                        err -> event.reply("Failed to send message: " + err.getMessage()).setEphemeral(true).queue()
                );
    }


    private String safeTrimForCodeblock(String s) {
        if (s == null) return "";
        String sanitized = s.replace("```", "\u200B```\u200B");
        return sanitized.length() > 4000 ? sanitized.substring(0, 4000) + "…" : sanitized;
    }
}