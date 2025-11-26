package com.zyx.pmBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
    public static void main(String[] args) throws Exception {
        BotConfig config = BotConfig.load();
        //TicketService ticketService = new TicketService(config);

        JDA jda = JDABuilder.createDefault(config.token())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setActivity(Activity.playing("zyx's bot"))
                .addEventListeners(
                        new SlashRegistrar(),
                        new AdPostHandler(config),
                        new SendHandler(config)
                        //new MassDoneFinishHandler(config, ticketService),
                        //new TicketButtonHandler(config, ticketService)
                )
                .build()
                .awaitReady();

        System.out.println("Bot ready: " + jda.getSelfUser().getAsTag());
    }
}
