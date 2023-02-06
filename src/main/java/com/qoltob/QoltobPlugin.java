package com.qoltob;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "QOL Tob"
)
public class QoltobPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private QoltobConfig config;

	private int currWave;

	private HashMap<Integer, ArrayList<Nylocas>> waveMap = new HashMap<Integer, ArrayList<Nylocas>>();

	private ArrayList<Nylocas> wave1 = new ArrayList<Nylocas>();

	@Override
	protected void startUp() throws Exception
	{
		currWave = 1;
		wave1.add(new Nylocas(Nylospawns.WEST_SOUTH,NyloStyle.RANGE_SMALL));
		wave1.add(new Nylocas(Nylospawns.SOUTH_EAST,NyloStyle.MAGE_SMALL));
		wave1.add(new Nylocas(Nylospawns.EAST_NORTH,NyloStyle.MELEE_SMALL));
		log.info(wave1.toString());

		log.info("started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("stopped!");
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		NPC npc = npcSpawned.getNpc();
		if (Objects.equals(npc.getName(), "Nylocas Ischyros") || Objects.equals(npc.getName(), "Nylocas Toxobolos")
				|| Objects.equals(npc.getName(), "Nylocas Hagios"))
		{
			WorldPoint location = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
			Point point = new Point(location.getRegionX(), location.getRegionY());
			Nylospawns nylospawn = Nylospawns.getLookup().get(point);
			if (nylospawn == null)
			{
				log.info("No spawnpoint found for " + npc.getName() + " with id: " + npc.getId());
				log.info("Location for the nylo is: " + point + " location " + location);
			}
			else
			{
				log.info("Nylo matched spawnpoints! It is: " + nylospawn);
				log.info(npc.getName() + " Loc is " + point +" location "+ location);
				Nylocas nylo = new Nylocas(nylospawn, NyloStyle.getLookup().get(npc.getId()));

				waveMap.computeIfAbsent(currWave, k -> new ArrayList<Nylocas>());
				waveMap.get(currWave).add(nylo);

				log.info(waveMap.get(currWave).toString());
				log.info(String.valueOf(currWave));

				if (waveMap.get(currWave).containsAll(wave1) && wave1.containsAll(waveMap.get(currWave)))
				{
					log.info("First wave filled!!");
					log.info(String.valueOf(currWave));
					currWave++;
					//TODO FIX WAVE MATCHING
					//after: hardcode rest of the waves
					//then count ticks a current wave has been active for.
					//then hardcode time inbetween waves (no stall)
					//calculate the difference to calculate stalls.
					//TODO Obviously check that youre in the nylocas room, otherwise dont start & reset...
				}
			}
		}
	}

	@Provides
	QoltobConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QoltobConfig.class);
	}
}
