package com.nylostats.events;

import lombok.Getter;
import net.runelite.api.NPC;

public class BossSpawnedEvent
{
    @Getter
    private NPC npc;

    public BossSpawnedEvent(NPC npc)
    {
        this.npc = npc;
    }
}
