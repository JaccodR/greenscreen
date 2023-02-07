package com.qoltob;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
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
	private int ticksSinceLastWave;
	private int meleeSplits;
	private int rangeSplits;
	private int mageSplits;
	private boolean inTob;
	private boolean bossSpawned;

	@Override
	protected void startUp() throws Exception
	{
		currWave = 0;
		meleeSplits = 0;
		rangeSplits = 0;
		mageSplits = 0;
		ticksSinceLastWave = 0;
		bossSpawned = false;

		log.info("started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		ticksSinceLastWave++;
		for (NPC npc : client.getNpcs())
		{
			if (!(Objects.equals(npc.getName(), "Nylocas Ischyros") || Objects.equals(npc.getName(), "Nylocas Toxobolos")
					|| Objects.equals(npc.getName(), "Nylocas Hagios")))
			{
				continue;
			}
			WorldPoint location = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
			Point point = new Point(location.getRegionX(), location.getRegionY());
			Nylospawns nylospawn = Nylospawns.getLookup().get(point);

			if (nylospawn != null && ticksSinceLastWave > 3)
			{
				log.info("ticks since last wave: " + ticksSinceLastWave);
				currWave++;
				log.info("Wave increased! now at wave: " + currWave);
				ticksSinceLastWave = 0;
				return;
			}
		}
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
				if (Objects.equals(npc.getName(), "Nylocas Ischyros"))
				{
					meleeSplits++;
				}
				else if (Objects.equals(npc.getName(), "Nylocas Toxobolos"))
				{
					rangeSplits++;
				}
				else if(Objects.equals(npc.getName(), "Nylocas Hagios"))
				{
					mageSplits++;
				}
			}
		}
	}
	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!inTob)
		{
			return;
		}
		NPC npc = npcDespawned.getNpc();
		if (Objects.equals(npc.getName(), "Nylocas Vasilias"))
		{
			if (!bossSpawned)
			{
				bossSpawned = true;
			}
			else
			{
				printSplits();
				log.info("Nylo waves finished! Waves: " + currWave);
				reset();
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		int tobVar = client.getVarbitValue(Varbits.THEATRE_OF_BLOOD);
		inTob = tobVar == 2 || tobVar == 3;
		if (!inTob)
		{
			reset();
		}

		int region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		int status = client.getVarbitValue(6447);
		if (region == 13122)
		{
			//xd
		}
	}

	@Provides
	QoltobConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QoltobConfig.class);
	}

	private void printSplits()
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Melee: " + meleeSplits + " Range: " + rangeSplits + " Mage: " + mageSplits, "");
	}

	private void reset()
	{
		currWave = 0;
		meleeSplits = 0;
		rangeSplits = 0;
		mageSplits = 0;
		ticksSinceLastWave = 0;
		bossSpawned = false;
	}
}
