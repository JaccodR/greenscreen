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
import org.apache.commons.lang3.ArrayUtils;

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
	private int[] splits;
	private int[] preCapSplits;
	private int[] bossRotation;
	private boolean isHmt;
	private ArrayList<String> stallMessagesAll;
	private ArrayList<String> stallMessagesCollapsed;
	private static final Pattern NYLO_COMPLETE = Pattern.compile("Wave 'The Nylocas' \\(.*\\) complete!");
	private final int NYLOCAS_REGIONID = 13122;


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
		ticksSinceLastWave = 0;
		stalls = 0;
		isHmt = false;
		stallMessagesAll = new ArrayList<>();
		stallMessagesCollapsed = new ArrayList<>();
		splits = new int[3];
		preCapSplits = new int[3];
		bossRotation = new int[3];
		bossRotation[0] = 1;
	}

	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inNyloRegion())
		{
			return;
		}

		if (ticksSinceLastWave % 4 == 0)
		{
			if (currWave > 1 && currWave < 31 && ticksSinceLastWave >= waveNaturalStalls.get(currWave))
			{
				int curCap = 12;
				if (currWave > 19)
					curCap = 24;

				if (isHmt)
					curCap += 0; //TODO figure out and fix this shit for hmt

				int aliveNylos = 0;
				for (NPC npc : client.getNpcs())
				{
					if (Objects.equals(npc.getName(), "Nylocas Ischyros") || Objects.equals(npc.getName(), "Nylocas Toxobolos")
							|| Objects.equals(npc.getName(), "Nylocas Hagios"))
					{
						aliveNylos++;
					}
					if (Objects.equals(npc.getName(), "Nylocas Prinkipas"))
					{
						aliveNylos += 3;
					}
				}

				if (aliveNylos >= curCap)
				{
					String stallMsg = "Stalled wave: <col=EF1020>" + currWave + "/31</col>";

					if (config.showStalls() == StallDisplays.ALL_ALIVE || config.showStalls() == StallDisplays.ALL_ALIVE_TOTAL)
						stallMsg += " - Nylos alive: <col=EF1020>" + aliveNylos + "/" + curCap + "</col>";
					if (config.showStalls() == StallDisplays.ALL_ALIVE_TOTAL)
						stallMsg += " - Total Stalls: <col=EF1020>" + (stallMessagesAll.size() + 1) + "</col>";

					stallMessagesAll.add(stallMsg);
				}
			}
		}
		ticksSinceLastWave++;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
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
					splits[0]++;
				else if (npc.getId() == 8343 || npc.getId() == 10775 || npc.getId() == 10792)
					splits[1]++;
				else if(npc.getId() == 8344 || npc.getId() == 10776 || npc.getId() == 10793)
					splits[2]++;

				if (npc.getId() == 10791 || npc.getId() == 10792 || npc.getId() == 17093)
					isHmt = true;
			}
			else
			{
				if (ticksSinceLastWave > 3)
				{
					if (currWave > 1 && (ticksSinceLastWave - waveNaturalStalls.get(currWave)) > 0)
					{
						int stallAmount = (ticksSinceLastWave - waveNaturalStalls.get(currWave)) / 4;
						stalls += stallAmount;
						if (stallAmount == 1)
							stallMessagesCollapsed.add("Stalled wave: <col=EF1020>" + currWave + "/31</col> - <col=EF1020>" + stallAmount + "</col> time");
						else
							stallMessagesCollapsed.add("Stalled wave: <col=EF1020>" + currWave + "/31</col> - <col=EF1020>" + stallAmount + "</col> times");
					}
					currWave++;
					if (currWave == 20)
						preCapSplits = splits.clone();

					ticksSinceLastWave = 0;
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inNyloRegion() || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		String msg = Text.removeTags(event.getMessage());
		if (NYLO_COMPLETE.matcher(msg).find())
		{
			if (currWave != 31)
			{
				reset();
				return;
			}
			if (config.showStalls() != StallDisplays.OFF)
			{
				printStalls();
			}
			if (config.showTotalStalls())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Total stalled waves: <col=EF1020>" + stalls + "</col>", "");
			}
			if (config.showSplits())
			{
				printSplits();
			}
			if (config.showBossRotation())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Boss rotation: [<col=EF1020>" + bossRotation[0] +
						"</col>] [<col=00FF0A>" + bossRotation[2] + "</col>] [<col=2536CA>" + bossRotation[1] + "</col>]", "");
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
		boolean inTob = tobVar == 2 || tobVar == 3;
		if (!inTob)
		{
			reset();
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged npcChanged)
	{
		int npcId = npcChanged.getNpc().getId();

		switch(npcId)
		{
			case 8355:
			case 10787:
			case 10808:
				bossRotation[0]++;
				break;
			case 8356:
			case 10788:
			case 10809:
				bossRotation[1]++;
				break;
			case 8357:
			case 10789:
			case 10810:
				bossRotation[2]++;
				break;
		}
	}

	private boolean inNyloRegion()
	{
		return ArrayUtils.contains(client.getMapRegions(), NYLOCAS_REGIONID);
	}

	@Provides
	NyloStatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NyloStatsConfig.class);
	}

	private void printSplits()
	{

		String msgCap = "Pre cap splits: [<col=EF1020>" + preCapSplits[0] +
				"</col>] [<col=00FF0A>" + preCapSplits[1] + "</col>] [<col=2536CA>" + preCapSplits[2] + "</col>]";
		msgCap +=" Post cap splits: [<col=EF1020>" + (splits[0] - preCapSplits[0]) +
				"</col>] [<col=00FF0A>" + (splits[1] - preCapSplits[1]) + "</col>] [<col=2536CA>" + (splits[2] - preCapSplits[2]) + "</col>]";

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msgCap, "");

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",  "Total splits: [<col=EF1020>" + splits[0] +
		"</col>] [<col=00FF0A>" + splits[1] + "</col>] [<col=2536CA>" + splits[2] + "</col>]", "");
	}

	private void printStalls()
	{
		if (config.showStalls() == StallDisplays.ALL || config.showStalls() == StallDisplays.ALL_ALIVE || config.showStalls() == StallDisplays.ALL_ALIVE_TOTAL)
		{
			for (String msg : stallMessagesAll)
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
		}
		else if (config.showStalls() == StallDisplays.COLLAPSED)
		{
			for (String msg : stallMessagesCollapsed)
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
		}
	}

	private void reset()
	{
		currWave = 0;
		ticksSinceLastWave = 0;
		stalls = 0;
		isHmt = false;
		stallMessagesAll.clear();
		stallMessagesCollapsed.clear();
		splits = new int[3];
		preCapSplits = new int[3];
		bossRotation = new int[3];
		bossRotation[0] = 1;
	}
}
