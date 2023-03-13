package com.nylostats;

import com.nylostats.events.BossSpawnedEvent;
import com.nylostats.events.NyloStartedEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
public class NyloPlotHandler
{
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    private final NyloStatsPlugin plugin;
    private final NyloStatsConfig config;
    private boolean active;
    @Getter
    private ArrayList<Integer> nylosAlive;

    @Inject
    protected NyloPlotHandler(NyloStatsPlugin plugin, NyloStatsConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        active = false;
        nylosAlive = new ArrayList<>();
    }

    @Subscribe
    protected void onNyloStartedEvent(NyloStartedEvent event)
    {
        active = true;
    }

    @Subscribe
    protected void onBossSpawnedEvent(BossSpawnedEvent event)
    {
        active = false;
    }

    public void reset()
    {
        active = false;
        nylosAlive.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!active)
            return;

        int aliveNylos = 0;
        for (NPC npc : client.getNpcs())
        {
            if (Objects.equals(npc.getName(), "Nylocas Ischyros") || Objects.equals(npc.getName(), "Nylocas Toxobolos") || Objects.equals(npc.getName(), "Nylocas Hagios"))
                aliveNylos++;
            if (Objects.equals(npc.getName(), "Nylocas Prinkipas"))
                aliveNylos += 3; // nylo prince adds 3 to the cap
        }
        nylosAlive.add(aliveNylos);
    }
}
