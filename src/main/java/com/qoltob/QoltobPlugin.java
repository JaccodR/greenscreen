package com.qoltob;

import com.google.inject.Provides;

import javax.annotation.concurrent.Immutable;
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
	private int stalls;
	private int ticksSinceLastWave;
	private int meleeSplits;
	private int rangeSplits;
	private int mageSplits;
	private boolean inTob;
	private boolean bossSpawned;
	private ArrayList<String> stallMessages;

	private static final HashMap<Integer, Integer> waveNaturalStalls;
	static
	{
		waveNaturalStalls = new HashMap<>();
		waveNaturalStalls.put(1,4);
		waveNaturalStalls.put(2,4);
		waveNaturalStalls.put(3,4);
		waveNaturalStalls.put(4,4);
		waveNaturalStalls.put(5,16);

		waveNaturalStalls.put(6,4);
		waveNaturalStalls.put(7,12);
		waveNaturalStalls.put(8,4);
		waveNaturalStalls.put(9,12);
		waveNaturalStalls.put(10,8);

		waveNaturalStalls.put(11,8);
		waveNaturalStalls.put(12,8);
		waveNaturalStalls.put(13,8);
		waveNaturalStalls.put(14,8);
		waveNaturalStalls.put(15,8);

		waveNaturalStalls.put(16,4);
		waveNaturalStalls.put(17,12);
		waveNaturalStalls.put(18,8);
		waveNaturalStalls.put(19,12);
		waveNaturalStalls.put(20,16);

		waveNaturalStalls.put(21,8);
		waveNaturalStalls.put(22,12);
		waveNaturalStalls.put(23,8);
		waveNaturalStalls.put(24,8);
		waveNaturalStalls.put(25,8);

		waveNaturalStalls.put(26,4);
		waveNaturalStalls.put(27,8);
		waveNaturalStalls.put(28,4);
		waveNaturalStalls.put(29,4);
		waveNaturalStalls.put(30,4);
	}

	@Override
	protected void startUp() throws Exception
	{
		currWave = 0;
		meleeSplits = 0;
		rangeSplits = 0;
		mageSplits = 0;
		ticksSinceLastWave = 0;
		stalls = 0;
		bossSpawned = false;
		stallMessages = new ArrayList<String>();

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
				if (currWave > 1 && (ticksSinceLastWave - waveNaturalStalls.get(currWave)) > 0)
				{
					int stallamount = (ticksSinceLastWave - waveNaturalStalls.get(currWave)) / 4;
					stalls += stallamount;

					for (int i = 0; i < stallamount; i++)
					{
						stallMessages.add("Stalled wave: <col=EF1020>" + currWave + "/31");
					} //TODO potentially total stalls at the end of msg?
				}
				currWave++;
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
				if (npc.getId() == 8342)
				{ //TODO add hardmode/entry mode ids...
					meleeSplits++;
				}
				else if (npc.getId() == 8343)
				{
					rangeSplits++;
				}
				else if(npc.getId() == 8344)
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
				printStalls();
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
	}

	@Provides
	QoltobConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QoltobConfig.class);
	}

	private void printSplits()
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Total splits: [<col=EF1020>" + meleeSplits +
				"<col=00>] [<col=00FF0A>" + rangeSplits + "<col=00>] [<col=2536CA>" + mageSplits + "<col=00>]", "");
	}

	private void printStalls()
	{
		for (String msg : stallMessages)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Total stalled waves: <col=EF1020>" + stalls, "");
	}

	private void reset()
	{
		currWave = 0;
		meleeSplits = 0;
		rangeSplits = 0;
		mageSplits = 0;
		ticksSinceLastWave = 0;
		stalls = 0;
		bossSpawned = false;
		stallMessages = new ArrayList<String>();
	}
}
