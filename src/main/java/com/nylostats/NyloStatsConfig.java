package com.nylostats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("nylostats")
public interface NyloStatsConfig extends Config
{
    @ConfigItem(
            position =  0,
            keyName = "showTotalStalls",
            name = "Display total stalls",
            description = "Shows total number of stalls after the nylocas room is completed."
    )
    default boolean showTotalStalls()
    {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "showStalls",
            name = "Display all stalls",
            description = "Shows all waves you have stalled after the nylocas room is completed."
    )
    default StallDisplays showStalls()
    {
        return StallDisplays.OFF;
    }

    @ConfigItem(
            position = 2,
            keyName = "smallSplits",
            name = "Display small splits",
            description = "Shows the amount of nylocas that have spawned from bigs."
    )
    default boolean showSplits()
    {
        return false;
    }
}
