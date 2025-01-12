package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeployableOverlay {

	private static Deployable current = null;
	private static final Pattern deployablePattern = Pattern.compile("^([A-Za-z ]+) (\\d{1,3})s$"); // e.g. "Radiant 20s"

	public static final Map<String, DeployableType> deployablesMap = new Object2ObjectOpenHashMap<>(); // todo correct hashmap type?

	static {
		for (DeployableType type : DeployableType.values()) { // grab all the custom skull models from the skull textures to display on players hud
			type.stack = new ItemStack(Items.PLAYER_HEAD);
			type.stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.of(UUID.randomUUID()),
					ItemUtils.propertyMapWithTexture(type.skullTexture)
			));

			deployablesMap.put(type.name, type);
		}
	}


	@Init
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) {
				return;
			}

			if (!SkyblockerConfigManager.get().uiAndVisuals.deployableOverlayConfig.enabled) {
				current = null;
				return;
			}

			// todo plasmaflux is 20 blocks not 18
			List<ArmorStandEntity> armorStands = client.world.getEntitiesByClass(ArmorStandEntity.class, client.player.getBoundingBox().expand(18d), EntityPredicates.NOT_MOUNTED);
			refreshDeployable(armorStands);

		});

		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickDelta) -> {
			if (current == null || current.timeLeft < 0) {
				return;
			}

			// todo right way to get textRenderer?
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

			context.drawCenteredTextWithShadow(textRenderer, current.type.name, 110, 100, getNameColor());
			context.drawCenteredTextWithShadow(textRenderer, current.timeLeft + "s", 110, 110, getTimerColor());


			// scale skull up by 2x
			MatrixStack stack = context.getMatrices();

			// todo config option for coords
			stack.push();
			stack.translate(92, 68, 0);
			stack.scale(2f, 2f, 1f);
			context.drawItem(current.type.stack, 0, 0);

			stack.pop();
		});
	}

	private static void refreshDeployable(List<ArmorStandEntity> armorStands) {
		current = null;

		for (ArmorStandEntity armorStand : armorStands) {

			String name = armorStand.getName().getString();
			if (!name.endsWith("s")) { // nametag should end with 's' for num of seconds left
				continue;
			}

			Matcher deployableMatcher = deployablePattern.matcher(name);
			if (!deployableMatcher.find()) {
				continue;
			}

			DeployableType type = deployablesMap.get(deployableMatcher.group(1));
			int timeLeft = Integer.parseInt(deployableMatcher.group(2));


			if (type != null && (current == null || type.priority > current.type.priority)) {
				current = new Deployable(type, timeLeft);
			}
		}
	}


	private record Deployable(DeployableType type, int timeLeft) {}

	public enum DeployableType {
		RADIANT("Radiant", 1, null, "ewogICJ0aW1lc3RhbXAiIDogMTYwNzQ0Nzk4NTQxNCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk0ZDBhMDY4ZWE1MGE5ZGUyY2VmMjNhOTJiY2E0YjM2NzhkMTJjYThhMTgxNWQxM2JlYWM5NGRmZDU1NzEyNSIKICAgIH0KICB9Cn0="),
		MANA_FLUX("Mana Flux", 2, null, "ewogICJ0aW1lc3RhbXAiIDogMTYyMTM0MjI5MzI5NiwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyYWRhMWM3ZmNjOGNmMzVkZWZlYjk0NGE0ZjhmZmE5YTlkMjYwNTYwZmM3ZjVmNTgyNmRlODA4NTQzNTk2N2MiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
		OVER_FLUX("Overflux", 3, null, "ewogICJ0aW1lc3RhbXAiIDogMTcwODY4ODA2MjE4OCwKICAicHJvZmlsZUlkIiA6ICIzNzRhZGZlMjkyOWI0ZDBiODJmYmVjNTg2ZTI5ODk4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfR2xvenpfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIwZGU1ZTg5NzQ5NDAzNzU5MzRkMzJmNzFjOTFhZDJkNTcyOGQzOGU1MTY0N2RjYzhmMzkyMDZjMDk5YTU0YzIiCiAgICB9CiAgfQp9"),
		PLASMA_FLUX("Plasmaflux", 4, null, ""); // todo get this

		private final int priority;
		private ItemStack stack;
		private final String skullTexture;
		private final String name;

		DeployableType(String name, int priority, ItemStack stack, String skullTexture) {
			this.priority = priority;
			this.stack = stack;
			this.skullTexture = skullTexture;
			this.name = name;
		}
	}


	private static int getTimerColor() {
		Formatting timeColor;
		if (current.timeLeft > 30) {
			timeColor = Formatting.GREEN;
		} else if (current.timeLeft > 10) {
			timeColor = Formatting.YELLOW;
		} else {
			timeColor = Formatting.RED;
		}
		return timeColor.getColorValue();
	}

	private static int getNameColor() {
		Formatting nameColor = switch (current.type) {
			case RADIANT -> Formatting.GREEN;
			case MANA_FLUX -> Formatting.BLUE;
			case OVER_FLUX -> Formatting.DARK_PURPLE;
			case PLASMA_FLUX -> Formatting.GOLD;
		};
		return nameColor.getColorValue();
	}
}
