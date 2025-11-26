package com.zyx.pmBot;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SlashRegistrar extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var jda = event.getJDA();

        jda.updateCommands().queue(); // löscht global
        jda.getGuilds().forEach(g -> g.updateCommands().queue()); // löscht pro Guild

        List<CommandData> cmds = new ArrayList<>();
        cmds.add(
                Commands.slash("adpost", "Open a form to post normal + codeblock ad into a channel")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Target text channel", true)
                                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS))
        );
        cmds.add(
                Commands.slash("send", "Send normal messages and/or embeds via a form")
                        .addSubcommands(
                                new SubcommandData("message_only", "Send a normal message only")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Target channel", true)
                                                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)),
                                new SubcommandData("embed_only", "Send an embed only")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Target channel", true)
                                                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)),
                                new SubcommandData("message_and_embed", "Send both message and embed")
                                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Target channel", true)
                                                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS))
                        )
        );
        cmds.addAll(Arrays.asList(MassDoneFinishHandler.commandData()));

        jda.getGuilds().forEach(g ->
                g.updateCommands().addCommands(cmds).queue(
                        s -> System.out.println("✅ Commands registered in: " + g.getName()),
                        e -> System.out.println("❌ Failed in " + g.getName() + ": " + e)
                )
        );

    }
}