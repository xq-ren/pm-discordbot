package com.zyx.pmBot;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Set;

public record BotConfig(
        String token,
        Set<String> allowedRoleIds,
        ServerConfig server
) {
    public static BotConfig load() {
        try {
            return new ObjectMapper().readValue(new File("config.json"), BotConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.json", e);
        }
    }

    public record ServerConfig(
            String ticketsCategoryId,
            String logChannelId,
            String queueChannelId,
            String doneChannelId,
            String mrevChannelId,
            String accessServerLink
    ) {}
}