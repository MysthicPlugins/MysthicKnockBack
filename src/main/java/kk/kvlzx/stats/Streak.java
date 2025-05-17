package kk.kvlzx.stats;

import net.md_5.bungee.api.ChatColor;

public class Streak {

    private int kills;

    public Streak() {
        this.kills = 0;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public void reset() {
        kills = 0;
    }

    public boolean isStreak() {
        return kills > 0 && kills % 5 == 0;
    }

    public String getMvpTag() {
        if (kills >= 500) {
            return ChatColor.DARK_PURPLE + "MVP+";
        } else if (kills >= 300) {
            return ChatColor.RED + "MVP";
        } else if (kills >= 250){
            return ChatColor.GOLD + "MVP";
        } else if (kills >= 200) {
            return ChatColor.YELLOW + "MVP";
        } else if (kills >= 150) {
            return ChatColor.AQUA + "MVP";
        } else if (kills >= 100){
            return ChatColor.GREEN + "MVP";
        } else if (kills >= 80){
            return ChatColor.BLUE + "MVP";
        } else if (kills >= 60){
            return ChatColor.LIGHT_PURPLE + "MVP";
        } else if (kills >= 40){
            return ChatColor.GRAY + "MVP";
        }
        return null;
    }
}

