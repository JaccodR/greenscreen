package com.nylostats;

import com.google.inject.Provides;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Nylo Stats"
)
public class NyloStatsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NyloStatsConfig config;

	private int currWave;
	private int stalls;
	private int ticksSinceLastWave;
	private int meleeSplits;
	private int rangeSplits;
	private int mageSplits;
	private boolean inTob;
	private ArrayList<String> stallMessagesAll;
	private ArrayList<String> stallMessagesCollapsed;
	private static final Pattern NYLO_COMPLETE = Pattern.compile("Wave 'The Nylocas' \\(.*\\) complete!");

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
		stallMessagesAll = new ArrayList<String>();
		stallMessagesCollapsed = new ArrayList<String>();
	}

	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inTob)
		{
			return;
		}

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
						stallMessagesAll.add("Stalled wave: <col=EF1020>" + currWave + "/31");
					}

					if (stallamount == 1)
					{
						stallMessagesCollapsed.add("Stalled wave: <col=EF1020>" + currWave + "/31<col=00> - <col=EF1020>" + stallamount + "<col=00> time");
					}
					else
					{
						stallMessagesCollapsed.add("Stalled wave: <col=EF1020>" + currWave + "/31<col=00> - <col=EF1020>" + stallamount + "<col=00> times");
					}
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
				if (npc.getId() == 8342 || npc.getId() == 10774 || npc.getId() == 10791)
				{
					meleeSplits++;
				}
				else if (npc.getId() == 8343 || npc.getId() == 10775 || npc.getId() == 10792)
				{
					rangeSplits++;
				}
				else if(npc.getId() == 8344 || npc.getId() == 10776 || npc.getId() == 10793)
				{
					mageSplits++;
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inTob || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		String msg = Text.removeTags(event.getMessage());
		if (NYLO_COMPLETE.matcher(msg).find())
		{
			if (config.showSplits())
			{
				printSplits();
			}
			if (config.showStalls() != StallDisplays.OFF)
			{
				printStalls();
			}
			if (config.showTotalStalls())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Total stalled waves: <col=EF1020>" + stalls, "");
			}
			reset();
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
	NyloStatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NyloStatsConfig.class);
	}

	private void printSplits()
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Total splits: [<col=EF1020>" + meleeSplits +
				"<col=00>] [<col=00FF0A>" + rangeSplits + "<col=00>] [<col=2536CA>" + mageSplits + "<col=00>]", "");
	}

	private void printStalls()
	{
		if (config.showStalls() == StallDisplays.ALL)
		{
			for (String msg : stallMessagesAll)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
			}
		}
		else if (config.showStalls() == StallDisplays.COLLAPSED)
		{
			for (String msg : stallMessagesCollapsed)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
			}
		}
	}

	private void reset()
	{
		currWave = 0;
		meleeSplits = 0;
		rangeSplits = 0;
		mageSplits = 0;
		ticksSinceLastWave = 0;
		stalls = 0;
		stallMessagesAll = new ArrayList<String>();
		stallMessagesCollapsed = new ArrayList<String>();
	}
}
