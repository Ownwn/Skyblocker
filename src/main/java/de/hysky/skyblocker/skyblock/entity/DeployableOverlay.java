package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Colors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeployableOverlay {

	private static Deployables current = Deployables.NONE;
	private static Pattern deployablePattern = Pattern.compile("^([A-Za-z ]+) (\\d{1,3})s$");

	@Init
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) {
				return;
			}

			// todo plasmaflux is 20 blocks not 18
			// todo change to 18 not 8
			List<ArmorStandEntity> armorStands = client.world.getEntitiesByClass(ArmorStandEntity.class, client.player.getBoundingBox().expand(8d), EntityPredicates.NOT_MOUNTED);
			refreshDeployable(armorStands);

		});

		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickDelta) -> {
			if (current == Deployables.NONE) {
				return;
			}

			context.drawText(MinecraftClient.getInstance().textRenderer, current.name(), 100, 100, Colors.GREEN, true);
		});
	}

	private static void refreshDeployable(List<ArmorStandEntity> armorStands) {
		current = Deployables.NONE;

		for (ArmorStandEntity armorStand : armorStands) {
			String name = armorStand.getName().getString();
			if (!name.endsWith("s")) { // nametag should end with 's' for num of seconds left
				System.out.println(name);
				return;
			}

			Matcher deployableMatcher = deployablePattern.matcher(name);
			if (!deployableMatcher.find()) {
				return;
			}

			Deployables deployable = deployablesMap.get(deployableMatcher.group(1));
			int timeLeft = Integer.parseInt(deployableMatcher.group(2));

			if (deployable != null && deployable.priority > current.priority) {
				current = deployable;
			}
		}
	}

	public static final Map<String, Deployables> deployablesMap = new HashMap<>() {{
		put("Radiant", Deployables.RADIANT);
		put("Mana Flux", Deployables.MANA_FLUX);
		put("Overflux", Deployables.OVER_FLUX);
	}};

	public enum Deployables {
		NONE(-1),
		RADIANT(1),
		MANA_FLUX(2),
		OVER_FLUX(3),
		PLASMA_FLUX(4);

		private final int priority;

		Deployables(int priority) {
			this.priority = priority;
		}
	}
}
