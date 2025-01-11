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

public class DeployableOverlay {

	private static Deployables current = Deployables.NONE;

	@Init
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) {
				return;
			}

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
			Iterable<ItemStack> equippedItems = armorStand.getEquippedItems();

			for (ItemStack stack : equippedItems) {
				Optional<String> textureOptional = ItemUtils.getHeadTextureOptional(stack);
				if (textureOptional.isPresent()) {
					String texture = textureOptional.get();
					texture = texture.substring(0, texture.length() - 1); // has '=' at end. todo what am i doing wrong

					Deployables deployable = deployablesMap.get(texture);

					if (deployable != null && deployable.priority > current.priority) {
						current = deployable;
					}
				}
			}
		}
	}

	public static final Map<String, Deployables> deployablesMap = new HashMap<>() {{
		put("ewogICJ0aW1lc3RhbXAiIDogMTYwNzQ0Nzk4NTQxNCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk0ZDBhMDY4ZWE1MGE5ZGUyY2VmMjNhOTJiY2E0YjM2NzhkMTJjYThhMTgxNWQxM2JlYWM5NGRmZDU1NzEyNSIKICAgIH0KICB9Cn0"
				, Deployables.RADIANT);

		put("ewogICJ0aW1lc3RhbXAiIDogMTYyMTM0MjI5MzI5NiwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyYWRhMWM3ZmNjOGNmMzVkZWZlYjk0NGE0ZjhmZmE5YTlkMjYwNTYwZmM3ZjVmNTgyNmRlODA4NTQzNTk2N2MiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ"
				, Deployables.MANA_FLUX);
	}};

	public enum Deployables {
		NONE(-1),
		RADIANT(1),
		MANA_FLUX(2);

		private final int priority;

		Deployables(int priority) {
			this.priority = priority;
		}
	}
}
