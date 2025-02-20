package io.github.prospector.modmenu.util.mod;

import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.ModsScreen;
import io.github.prospector.modmenu.util.DrawingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class ModBadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected Mod mod;
	protected MinecraftClient client;
	protected final ModsScreen screen;

	public ModBadgeRenderer( int startX, int startY, int endX, Mod mod, ModsScreen screen ) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = MinecraftClient.getInstance();
	}

	public void draw( int mouseX, int mouseY ) {
		this.badgeX = startX;
		this.badgeY = startY;
		mod.getBadges().forEach( badge -> drawBadge( badge, mouseX, mouseY ) );
		if ( ModMenuConfig.EASTER_EGGS.getValue() ) {
			//noinspection MagicConstant
			if ( Calendar.getInstance().get( 0b10 ) == 0b11 && Calendar.getInstance().get( 0b101 ) == 0x1 ) {
				if ( mod.getId().equals( new String( new byte[] { 109, 111, 100, 109, 101, 110, 117 }, StandardCharsets.UTF_8 ) ) ) {
					drawBadge( new LiteralText( new String( new byte[] { -30, -100, -104, 32, 86, 105, 114, 117, 115, 32, 68, 101, 116, 101, 99, 116, 101, 100 }, StandardCharsets.UTF_8 ) ), 0b10001000111111110010001000100010, 0b10001000011111110000100000001000, mouseX, mouseY );
				} else if ( mod.getId().contains( new String( new byte[] { 116, 97, 116, 101, 114 }, StandardCharsets.UTF_8 ) ) ) {
					drawBadge( new LiteralText( new String( new byte[] { 116, 97, 116, 101, 114 }, StandardCharsets.UTF_8 ) ), 0b10001000111010111011001100101011, 0b10001000100110010111000100010010, mouseX, mouseY );
				} else {
					drawBadge( new LiteralText( new String( new byte[] { -30, -100, -108, 32, 98, 121, 32, 77, 99, 65, 102, 101, 101 }, StandardCharsets.UTF_8 ) ), 0b10001000000111011111111101001000, 0b10001000000001110110100100001110, mouseX, mouseY );
				}
			}
		}
	}

	public void drawBadge( Mod.Badge badge, int mouseX, int mouseY ) {
		this.drawBadge( badge.getText(), badge.getOutlineColor(), badge.getFillColor(), mouseX, mouseY );
	}

	public void drawBadge( Text text, int outlineColor, int fillColor, int mouseX, int mouseY ) {
		int width = client.textRenderer.getStringWidth( text.asFormattedString() ) + 6;
		if ( badgeX + width < badgeMax ) {
			DrawingUtil.drawBadge( badgeX, badgeY, width, text.asFormattedString(), outlineColor, fillColor, 0xCACACA );
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
