package io.github.prospector.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.widget.ModListWidget;
import io.github.prospector.modmenu.util.mod.ModSearch;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final Identifier PARENT_MOD_TEXTURE = new Identifier( ModMenu.MOD_ID, "textures/gui/parent_mod.png" );
	protected List<Mod> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry( Mod parent, List<Mod> children, ModListWidget list ) {
		super( parent, list );
		this.children = children;
		this.list = list;
	}

	@Override
	public void render( int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered ) {
		super.render( index, x, y, rowWidth, rowHeight, mouseX, mouseY, hovered );
		TextRenderer textRenderer = client.textRenderer;
		int childrenBadgeHeight = textRenderer.fontHeight;
		//noinspection SuspiciousNameCombination
		int childrenBadgeWidth = textRenderer.fontHeight;
		int children = ModSearch.search( list.getParent(), list.getParent().getSearchInput(), getChildren() ).size();
		int childrenWidth = textRenderer.getStringWidth( Integer.toString( children ) ) - 1;

		if ( childrenBadgeWidth < childrenWidth + 4 )
			childrenBadgeWidth = childrenWidth + 4;

		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0x8810d098;
		int childrenFillColor = 0x88046146;
		DrawableHelper.fill( childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenOutlineColor );
		DrawableHelper.fill( childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor );
		DrawableHelper.fill( childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, childrenOutlineColor );
		DrawableHelper.fill( childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, childrenFillColor );
		DrawableHelper.fill( childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, childrenOutlineColor );
		textRenderer.draw( Integer.toString( children ), childrenBadgeX + childrenBadgeWidth / 2 - childrenWidth / 2, childrenBadgeY + 1, 0xCACACA );

		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if ( this.isMouseOver( mouseX, mouseY ) ) {
			DrawableHelper.fill( x, y, x + iconSize, y + iconSize, 0xA0909090 );
			int xOffset = list.getParent().showModChildren.contains( getMod().getId() ) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			this.client.getTextureManager().bindTexture( PARENT_MOD_TEXTURE );
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
			DrawableHelper.drawTexture(
				x,
				y,
				xOffset,
				yOffset,
				iconSize + xOffset,
				iconSize + yOffset,
				ModMenuConfig.COMPACT_LIST.getValue() ?
					(int) ( 256 / ( FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE ) ) :
					256,
				ModMenuConfig.COMPACT_LIST.getValue() ?
					(int) ( 256 / ( FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE ) ) :
					256
			);
		}
	}

	@Override
	public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
		if ( hoveringIcon ) {
			String id = getMod().getId();
			if ( list.getParent().showModChildren.contains( id ) ) {
				list.getParent().showModChildren.remove( id );
			} else {
				list.getParent().showModChildren.add( id );
			}
			list.filter( list.getParent().getSearchInput(), false );
		}
		return super.mouseClicked( index, mouseX, mouseY, button, x, y );
	}

	public void setChildren( List<Mod> children ) {
		this.children = children;
	}

	public void addChildren( List<Mod> children ) {
		this.children.addAll( children );
	}

	public void addChildren( Mod... children ) {
		this.children.addAll( Arrays.asList( children ) );
	}

	public List<Mod> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver( int mouseX, int mouseY ) {
		return Objects.equals( this.list.getEntryAtPos( mouseX, mouseY ), this );
	}
}
