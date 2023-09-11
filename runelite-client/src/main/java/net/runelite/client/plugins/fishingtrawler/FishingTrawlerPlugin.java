package net.runelite.client.plugins.fishingtrawler;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.game.npcoverlay.NpcOverlayService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.util.function.Function;

@Slf4j
@PluginDescriptor(
	name = "Fishing Trawler"
)
public class FishingTrawlerPlugin extends Plugin
{

	@Inject
	private Client client;

	private static final int FISHINGTRAWLER_REGION1 = 7499;
	private static final int FISHINGTRAWLER_REGION2 = 8011;

	private boolean inRun;

	@Inject
	private Notifier notifier;

	@Inject
	private NpcOverlayService npcOverlayService;

	@Override
	protected void startUp() throws Exception
	{
		npcOverlayService.registerHighlighter(isTentacle);
		inRun = false;
	}

	private final Function<NPC, HighlightedNpc> isTentacle = (npc) ->
	{
		if (npc.getId() == 10709)
		{
			return HighlightedNpc.builder()
					.npc(npc)
					.tile(true)
					.highlightColor(Color.RED.brighter())
					.render(n -> true)
					.outline(true)
					.name(true)
					.build();
		}
		return null;
	};

	private boolean isInFishingTrawlerRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			return client.getLocalPlayer().getWorldLocation().getRegionID() == FISHINGTRAWLER_REGION1 ||
					client.getLocalPlayer().getWorldLocation().getRegionID() == FISHINGTRAWLER_REGION2;
		}

		return false;
	}
	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if(isInFishingTrawlerRegion()) {
			if(!inRun) {
				inRun = true;
				notifier.notify("Get on the deck you dumb idiot!");
			}
		}

		if(!isInFishingTrawlerRegion()) {
			if(inRun) {
				inRun = false;
				notifier.notify("Yer off the boat, matey!");
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		int trawlerContribution = client.getVarbitValue(Varbits.FISHING_TRAWLER_ACTIVITY);
		if(trawlerContribution < 50) {
			if (npcSpawned.getNpc().getId() == 10708 || npcSpawned.getNpc().getId() == 10709) notifier.notify("Kraken's here!");
		}
	}
	@Override
	protected void shutDown() throws Exception
	{
		npcOverlayService.unregisterHighlighter(isTentacle);
	}
}