package com.nylostats;

import com.nylostats.data.NyloWave;
import com.nylostats.events.BossSpawnedEvent;
import com.nylostats.events.NyloStartedEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class IdleTicksHandler
{
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ItemManager itemManager;
    private final NyloStatsPlugin plugin;
    private final NyloStatsConfig config;
    private boolean active;
    private int tickDelay;
    @Getter
    private int idleTicks;
    private final Map<Skill, Integer> previousXpMap = new EnumMap<Skill, Integer>(Skill.class);
    private static final int SPELLBOOK_VARBIT = 4070;
    private static final int BLOWPIPE = ItemID.TOXIC_BLOWPIPE;
    private static final Set<Integer> THREETICKWEAPONS = new HashSet<>(Arrays.asList(
            ItemID.CHINCHOMPA_10033, ItemID.RED_CHINCHOMPA_10034, ItemID.BLACK_CHINCHOMPA,
            ItemID.SWIFT_BLADE, ItemID.HAM_JOINT));
    private static final Set<Integer> FIVETICKWEAPONS = new HashSet<>(Arrays.asList(
            ItemID.DINHS_BULWARK,
            ItemID.SCYTHE_OF_VITUR_UNCHARGED, ItemID.SCYTHE_OF_VITUR,
            ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED, ItemID.HOLY_SCYTHE_OF_VITUR,
            ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED, ItemID.SANGUINE_SCYTHE_OF_VITUR));

    private static final int BARRAGE_ANIMATION = 1979;


    @Inject
    protected IdleTicksHandler(NyloStatsPlugin plugin, NyloStatsConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        tickDelay = 0;
        idleTicks = 0;
        active = false;
    }

    @Subscribe
    protected void onNyloStartedEvent(NyloStartedEvent event)
    {
        active = true;
        clientThread.invoke(this::initPreviousXpMap);
    }

    @Subscribe
    protected void onBossSpawnedEvent(BossSpawnedEvent event)
    {
        active = false;
    }

    private void initPreviousXpMap()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            previousXpMap.clear();
        }
        else
        {
            for (final Skill skill: Skill.values())
            {
                previousXpMap.put(skill, client.getSkillExperience(skill));
            }
        }
    }

    @Subscribe
    public void onFakeXpDrop(FakeXpDrop event)
    {
        processXpDrop(event.getSkill());
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        processXpDrop(event.getSkill());
    }

    private void processXpDrop(Skill skill)
    {
        if (!plugin.inNyloRegion())
            return;

        Player player = client.getLocalPlayer();
        if (player == null)
            return;

        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null)
            return;

        if (player.getAnimation() == BARRAGE_ANIMATION)
        {
            tickDelay += 5;
            return;
        }

        int weapon = playerComposition.getEquipmentId(KitType.WEAPON);
        int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE.getId());

        if (weapon == BLOWPIPE)
        {
            tickDelay += 2;
            if (attackStyle == 1)
                tickDelay += 1;
        }
        else if (THREETICKWEAPONS.contains(weapon))
        {
            tickDelay += 3;
            if (skill == Skill.RANGED)
                tickDelay += 1;
        }
        else if (FIVETICKWEAPONS.contains(weapon))
        {
            tickDelay += 5;
        }
        else
        {
            tickDelay += 4;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!active)
            return;

        int aliveNpcs = 0;
        for (NPC npc: client.getNpcs())
        {
            if (Objects.equals(npc.getName(), "Nylocas Ischyros") || Objects.equals(npc.getName(), "Nylocas Toxobolos")
                    || Objects.equals(npc.getName(), "Nylocas Hagios"))
                aliveNpcs++;
        }
        if (aliveNpcs == 0)
            return;

        if (tickDelay > 0)
            tickDelay -= 1;
        else
            idleTicks += 1;
    }

    public void reset()
    {
        tickDelay = 0;
        idleTicks = 0;
        active = false;
    }
}
