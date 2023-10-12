package com.nylostats;

import com.nylostats.events.BossSpawnedEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class IdleTicksHandler
{
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    private final NyloStatsPlugin plugin;
    private final NyloStatsConfig config;
    private NPC npc;
    private boolean active;
    private int tickDelay;
    private Animation currAnimation;
    @Getter
    private int idleTicks;
    private final Map<Skill, Integer> previousXpMap = new EnumMap<Skill, Integer>(Skill.class);
    private static final Set<Integer> TWOTICKWEAPONS = new HashSet<>(Arrays.asList(
            ItemID.TOXIC_BLOWPIPE, ItemID.DRAGON_DART
    ));
    private static final Set<Integer> THREETICKWEAPONS = new HashSet<>(Arrays.asList(
            ItemID.CHINCHOMPA_10033, ItemID.RED_CHINCHOMPA_10034, ItemID.BLACK_CHINCHOMPA,
            ItemID.SWIFT_BLADE, ItemID.HAM_JOINT));
    private static final Set<Integer> FIVETICKWEAPONS = new HashSet<>(Arrays.asList(
            ItemID.DINHS_BULWARK, ItemID.ZARYTE_CROSSBOW,
            ItemID.TWISTED_BOW,
            ItemID.SCYTHE_OF_VITUR_UNCHARGED, ItemID.SCYTHE_OF_VITUR,
            ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED, ItemID.HOLY_SCYTHE_OF_VITUR,
            ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED, ItemID.SANGUINE_SCYTHE_OF_VITUR));

    private static final int CHALLY = ItemID.CRYSTAL_HALBERD;

    private static final int BARRAGE_ANIMATION = 1979;


    @Inject
    protected IdleTicksHandler(NyloStatsPlugin plugin, NyloStatsConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        tickDelay = 0;
        idleTicks = 0;
        currAnimation = Animation.UNKNOWN;
        active = false;
    }

    @Subscribe
    protected void onBossSpawnedEvent(BossSpawnedEvent event)
    {
        active = true;
        npc = event.getNpc();
        clientThread.invoke(this::initPreviousXpMap);
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
        if (!active)
            return;

        if (!plugin.inNyloRegion())
            return;

        if (skill != Skill.RANGED && skill != Skill.MAGIC && skill != Skill.STRENGTH && skill != Skill.ATTACK)
        {
            return;
        }

        Player player = client.getLocalPlayer();
        if (player == null)
            return;

        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null)
            return;

        int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);

        if (player.getAnimation() == BARRAGE_ANIMATION)
        {
            tickDelay += 5;
            return;
        }

        int weapon = playerComposition.getEquipmentId(KitType.WEAPON);
        currAnimation = Animation.valueOf(player.getAnimation());

        tickDelay += currAnimation.getAttackSpeed();

        log.info("Tick delay1: " + tickDelay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!active || npc == null)
            return;

        NPC nyloBoss = null;
        for (NPC npc : client.getNpcs())
        {
            if (Objects.equals(npc.getName(), "Nylocas Vasilas"))
            {
                nyloBoss = npc;
            }
        }

        if (!npc.isDead() || nyloBoss != null)
        {
            log.info("Idle ticks: " + idleTicks);
            log.info("Tick delay: " + tickDelay);
            if (tickDelay > 0)
                tickDelay -= 1;
            else
            {
                idleTicks += 1;
                log.info("Idle tick added, now: " + idleTicks);
            }
        }
        else
        {
            log.info("Setting active to false");
            active = false;
        }
    }

    public void reset()
    {
        tickDelay = 0;
        idleTicks = 0;
        npc = null;
        active = false;
    }

    @Getter
    @AllArgsConstructor
    private enum Animation
    {
        UNKNOWN(-2, -1),
        IDLE(-1, 1),
        CLAW_SCRATCH(393, 4),
        STAFF_BASH(414, 4),
        PUNCH(422, 4),
        KICK(423, 4),
        BOW(426, 5),
        CHALLY_JAB(428,7),
        CHALLY_SWIPE(440, 7),
        TRIDENT_SANG(1167, 4),
        CHALLY_SPEC(1203, 7),
        BARRAGE(1979, 5),
        INQ_MACE(4503, 4),
        BLOWPIPE(5061, 2),
        DINHS(7511, 5),
        CLAW_SPEC(7514, 4),
        CROSSBOW(7552, 5),
        CHINCHOMPA(7618, 3),
        SURGE(7855, 5),
        SCYTHE(8056, 5),
        RAPIER(8145, 4),
        SWIFT(8288, 3),
        SWIFT_SLASH(390, 3),
        DART(7554, 2),
        HAM_JOINT(401, 3),
        ZARYTE_CBOW(9168, 5),
        WHIP(1658, 4),
        VOIDWAKER(390, 4),
        VOIDWAKER_SPEC(1378, 4);



        private final int id;
        private final int attackSpeed;

        @Getter
        private static final HashMap<Integer, Integer> lookup;
        static
        {
            lookup = new HashMap<>();
            for (Animation animation : Animation.values())
            {
                lookup.put(animation.getId(), animation.getAttackSpeed());
            }
        }

        public static Animation valueOf(int id)
        {
            return Arrays.stream(values())
                    .filter(anim -> anim.id == id)
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }
}
