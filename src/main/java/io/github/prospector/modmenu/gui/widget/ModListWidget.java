package io.github.prospector.modmenu.gui.widget;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.ModsScreen;
import io.github.prospector.modmenu.gui.widget.entries.ChildEntry;
import io.github.prospector.modmenu.gui.widget.entries.IndependentEntry;
import io.github.prospector.modmenu.gui.widget.entries.ModListEntry;
import io.github.prospector.modmenu.gui.widget.entries.ParentEntry;
import io.github.prospector.modmenu.util.mod.ModIconHandler;
import io.github.prospector.modmenu.util.mod.ModSearch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.MathHelper;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ModListWidget extends BetterEntryListWidget<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean( "modmenu.debug" );

	private final Map<Path, NativeImageBackedTexture> modIconsCache = new HashMap<>();
	private final ModsScreen parent;
	private final Set<Mod> addedMods = new HashSet<>();
	private List<Mod> mods = null;
	private String selectedModId = null;
	private final ModIconHandler iconHandler = new ModIconHandler();

	public ModListWidget( MinecraftClient client, int width, int height, int y1, int y2, int entryHeight, String searchTerm, ModsScreen parent ) {
		super( client, width, height, y1, y2, entryHeight );
		this.parent = parent;
		this.filter( searchTerm, false );
		setScrollAmount( parent.getScrollPercent() * Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) ) );
	}

	public void setScrollAmount( double amount ) {
		scrollAmount = (float) amount;
		int denominator = Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) );
		if ( denominator <= 0 )
			parent.updateScrollPercent( 0 );
		else
			parent.updateScrollPercent( (float) getScrollAmount() / Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) ) );
	}

	public void select( ModListEntry entry ) {
		this.setSelected( entry );
	}

	@Override
	public void setSelected( ModListEntry entry ) {
		super.setSelected( entry );
		this.selectedModId = entry.getMod().getId();
		this.parent.updateSelectedEntry( this.getSelectedOrNull() );
	}

	@Override
	protected int getEntryCount() {
		return children().size();
	}

	@Override
	protected boolean isEntrySelected( int index ) {
		ModListEntry selected = getSelectedOrNull();
		return selected != null && selected.getMod().getId().equals( getEntry( index ).getMod().getId() );
	}

	@Override
	public int addEntry( ModListEntry entry ) {
		if ( addedMods.contains( entry.getMod() ) ) {
			return 0;
		}
		addedMods.add( entry.getMod() );
		int i = super.addEntry( entry );
		if ( entry.getMod().getId().equals( selectedModId ) ) {
			setSelected( entry );
		}
		return i;
	}

	@Override
	protected boolean removeEntry( ModListEntry entry ) {
		addedMods.remove( entry.getMod() );
		return super.removeEntry( entry );
	}

	@Override
	public ModListEntry remove( int index ) {
		addedMods.remove( getEntry( index ).getMod() );
		return super.remove( index );
	}

	public void reloadFilters() {
		filter( parent.getSearchInput(), true );
	}


	public void filter( String searchTerm, boolean refresh ) {
		this.clearEntries();
		addedMods.clear();
		Collection<Mod> mods = ModMenu.MODS.values()
			.stream()
			.filter( mod -> !ModMenuConfig.HIDDEN_MODS.getValue().contains( mod.getId() ) )
			.collect( Collectors.toList() );

		if ( DEBUG ) {
			mods = new ArrayList<>( mods );
		}

		if ( this.mods == null || refresh ) {
			this.mods = new ArrayList<>();
			this.mods.addAll( mods );
			this.mods.sort( ModMenuConfig.SORTING.getValue().getComparator() );
		}

		List<Mod> matched = ModSearch.search( this.parent, searchTerm, this.mods );

		for ( Mod mod : matched ) {
			String modId = mod.getId();

			// Hide parent lib mods when not searching, and the config is set to hide
			if ( mod.getBadges().contains( Mod.Badge.LIBRARY ) && !ModMenuConfig.SHOW_LIBRARIES.getValue() )
				continue;

			// only add parent and independent mods to the list, childs are handled by the parent itself
			if ( !ModMenu.PARENT_MAP.values().contains( mod ) ) {
				if ( ModMenu.PARENT_MAP.keySet().contains( mod ) ) {
					// A parent mod with children

					List<Mod> children = ModMenu.PARENT_MAP.get( mod );
					children.sort( ModMenuConfig.SORTING.getValue().getComparator() );
					ParentEntry parent = new ParentEntry( mod, children, this );
					this.addEntry( parent );

					// Add all the child mods when not searching
					if ( this.parent.showModChildren.contains( modId ) ) {
						List<Mod> validChildren = ModSearch.search( this.parent, searchTerm, children );
						for ( Mod child : validChildren )
							this.addEntry( new ChildEntry( child, parent, this, children.indexOf( child ) == children.size() - 1 ) );
					}
				} else {
					// A mod with no children
					this.addEntry( new IndependentEntry( mod, this ) );
				}
			}
		}

		if (
			parent.getSelectedEntry() != null &&
				!children().isEmpty() ||
				this.getSelectedOrNull() != null &&
					this.getSelectedOrNull().getMod() != parent.getSelectedEntry().getMod()
		) {
			for ( ModListEntry entry : children() ) {
				if ( entry.getMod().equals( parent.getSelectedEntry().getMod() ) )
					setSelected( entry );
			}
		} else {
			if ( getSelectedOrNull() == null && !children().isEmpty() && getEntry( 0 ) != null ) {
				setSelected( getEntry( 0 ) );
			}
		}

		if ( getScrollAmount() > Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) ) ) {
			setScrollAmount( Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) ) );
		}
	}

	public final ModListEntry getEntryAtPos( double x, double y ) {
		int int_5 = MathHelper.floor( y - (double) this.yStart ) - this.headerHeight + this.getScrollAmount() - 4;
		int index = int_5 / this.entryHeight;
		return x < (double) this.getScrollbarPosition() &&
			x >= (double) getRowLeft() &&
			x <= (double) ( getRowLeft() + getRowWidth() ) &&
			index >= 0 && int_5 >= 0 &&
			index < this.getEntryCount() ? children().get( index ) : null;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - ( Math.max( 0, this.getMaxPosition() - ( this.yEnd - this.yStart - 4 ) ) > 0 ? 18 : 12 );
	}

	//	@Override
	protected int getRowLeft() {
		return this.xStart + 6;
	}

	public int getWidth() {
		return this.width;
	}

	public int getTop() {
		return this.yStart;
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCount() {
		return children().size();
	}

	@Override
	public void close() {
		for ( NativeImageBackedTexture tex : this.modIconsCache.values() ) {
			tex.clearGlId();
		}
	}

	public int getDisplayedCountFor( Set<String> set ) {
		int count = 0;
		for ( ModListEntry c : children() ) {
			if ( set.contains( c.getMod().getId() ) ) {
				count++;
			}
		}
		return count;
	}

	NativeImageBackedTexture getCachedModIcon( Path path ) {
		return this.modIconsCache.get( path );
	}

	void cacheModIcon( Path path, NativeImageBackedTexture tex ) {
		this.modIconsCache.put( path, tex );
	}

	public Set<Mod> getCurrentModSet() {
		return addedMods;
	}

	public ModIconHandler getIconHandler() {
		return this.iconHandler;
	}
}
