package io.github.prospector.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.widget.BetterEntryListWidget;
import io.github.prospector.modmenu.gui.widget.ModListWidget;
import io.github.prospector.modmenu.util.DrawingUtil;
import io.github.prospector.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import static io.github.prospector.modmenu.util.TranslationUtil.hasTranslation;

public class ModListEntry extends BetterEntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier( "textures/misc/unknown_pack.png" );

	protected final MinecraftClient client;
	protected final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;

	public ModListEntry( Mod mod, ModListWidget list ) {
		this.mod = mod;
		this.list = list;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void updatePosition( int index, int x, int y ) { }

	@Override
	public void render( int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta ) {
		this.render( index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered );
	}

	@Override
	public void render( int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered ) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		if ( "java".equals( mod.getId() ) )
			DrawingUtil.drawRandomVersionBackground( mod, x, y, iconSize, iconSize );
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		this.bindIconTexture();
		GlStateManager.enableBlend();
		DrawableHelper.drawTexture( x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize );
		GlStateManager.disableBlend();
		String name = mod.getName();
		String trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if ( font.getStringWidth( name ) > maxNameWidth )
			trimmedName = font.trimToWidth( name, maxNameWidth - font.getStringWidth( "..." ) ) + "...";

		font.draw( trimmedName, x + iconSize + 3, y + 1, 0xFFFFFF );
		if ( !ModMenuConfig.HIDE_BADGES.getValue() )
			new ModBadgeRenderer( x + iconSize + 3 + font.getStringWidth( name ) + 2, y, x + rowWidth, mod, list.getParent() ).draw( mouseX, mouseY );
		if ( !ModMenuConfig.COMPACT_LIST.getValue() ) {
			String summary = mod.getSummary();
			String translatableSummaryKey = "modmenu.summaryTranslation." + mod.getId();
			String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
			if ( hasTranslation( translatableSummaryKey ) ) {
				summary = I18n.translate( translatableSummaryKey );
			} else if ( hasTranslation( translatableDescriptionKey ) ) {
				summary = I18n.translate( translatableDescriptionKey );
			}
			DrawingUtil.drawWrappedString( summary, ( x + iconSize + 3 + 4 ), ( y + client.textRenderer.fontHeight + 2 ), rowWidth - iconSize - 7, 2, 0x808080 );
		} else {
			DrawingUtil.drawWrappedString( mod.getPrefixedVersion(), ( x + iconSize + 3 ), ( y + client.textRenderer.fontHeight + 2 ), rowWidth - iconSize - 7, 2, 0x808080 );
		}
	}

	@Override
	public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
		list.select( this );
		return true;
	}

	@Override
	public void mouseReleased( int index, int mouseX, int mouseY, int button, int x, int y ) {
	}

	public Mod getMod() {
		return mod;
	}

	public void bindIconTexture() {
		if ( this.iconLocation == null ) {
			this.iconLocation = new Identifier( ModMenu.MOD_ID, mod.getId() + "_icon" );
			NativeImageBackedTexture icon = mod.getIcon( list.getIconHandler(), 64 * this.client.options.guiScale );
			//noinspection ConstantConditions
			if ( icon != null ) {
				this.client.getTextureManager().loadTexture( this.iconLocation, icon );
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		this.client.getTextureManager().bindTexture( this.iconLocation );
	}

	public int getXOffset() {
		return 0;
	}
}
