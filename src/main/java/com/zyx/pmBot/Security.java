package com.zyx.pmBot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;


public class Security {
    public static boolean canUse(Member member, BotConfig cfg) {
        if (member == null) return false;
        if (member.hasPermission(Permission.MANAGE_SERVER)) return true;
        return member.getRoles().stream().anyMatch(r -> cfg.allowedRoleIds().contains(r.getId()));
    }
}