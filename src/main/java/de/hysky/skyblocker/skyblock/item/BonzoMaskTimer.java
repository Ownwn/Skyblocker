package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Colors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BonzoMaskTimer {

	public static long timeLeft = 0;

	public static final String MESSAGE_END = "saved your life!";
	public static final Pattern MESSAGE_PATTERN = Pattern.compile("Your( ⚚)? Bonzo's Mask saved your life!|Second Wind Activated! Your Spirit Mask saved your life!");

	@Init
	public static void init() {
		ChatEvents.RECEIVE_STRING.register(message -> {

			if (!message.endsWith(MESSAGE_END)) {
				return;
			}

			Matcher matcher = MESSAGE_PATTERN.matcher(message);
			if (matcher.find()) {
				timeLeft = System.currentTimeMillis();
			}
		});


		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickDelta) -> {
			long timer = System.currentTimeMillis() - timeLeft;
			if (timer < 3000) {
				String text = Formatters.INTEGER_NUMBERS.format(30 - timer / 100); // number of ticks left todo different display options

				int x = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
				int y = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 15;
				context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x, y, Colors.BLUE);
			}
		});
	}
}
