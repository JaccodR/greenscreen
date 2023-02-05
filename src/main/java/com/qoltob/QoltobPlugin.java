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

	@Override
	protected void startUp() throws Exception
	{
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
				|| Objects.equals(npc.getName(), "Nylocas Hagios") || Objects.equals(npc.getName(), "Nylocas Matomenos"))
		{
			WorldPoint location = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
			Point point = new Point(location.getRegionX(), location.getRegionY());
			Nylospawns nylospawn = Nylospawns.getLookup().get(point);
			if (nylospawn == null)
			{
				log.info("No spawnpoint found for " + npc.getName() + " with id: " + npc.getId());
				log.info("Location for the nylo is: " + npc.getWorldLocation());
				log.info("Location for the nylo is: " + point + " location " + location);
			}
			else
			{
				log.info("Nylo matched spawnpoints! It is: " + nylospawn);
				log.info(npc.getName() + " Loc is " + point +" location "+ location);
				Nylocas nylo = new Nylocas(nylospawn, npc.getId());
				//TODO make DS with nylos, match to waves (hardcode waves)
				//TODO hardcode wave delays
			}
		}
	}

	@Provides
	QoltobConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QoltobConfig.class);
	}
}
