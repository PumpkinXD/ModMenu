package io.github.prospector.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.widget.*;
import io.github.prospector.modmenu.gui.widget.entries.ModListEntry;
import io.github.prospector.modmenu.util.DrawingUtil;
import io.github.prospector.modmenu.util.ScreenTexts;
import io.github.prospector.modmenu.util.TranslationUtil;
import io.github.prospector.modmenu.util.mod.ModBadgeRenderer;
import io.github.prospector.modmenu.util.mod.UrlUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PagedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModsScreen extends AbstractScreen {
	private static final Identifier FILTERS_BUTTON_LOCATION = new Identifier( ModMenu.MOD_ID, "textures/gui/filters_button.png" );
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier( ModMenu.MOD_ID, "textures/gui/configure_button.png" );

	private static final TranslatableText TOGGLE_FILTER_OPTIONS = new TranslatableText( "modmenu.toggleFilterOptions" );
	private static final TranslatableText CONFIGURE = new TranslatableText( "modmenu.configure" );

	private static final Logger LOGGER = LogManager.getLogger( "Mod Menu" );

	private BetterTextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private ModListWidget modList;
	private Text tooltip;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();

	public ModsScreen( Screen previousScreen ) {
		super( previousScreen );
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		this.modList.handleMouse();
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	public void init() {
		paneY = 48;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int searchBoxWidth = paneWidth - 32 - 22;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
		this.searchBox = new BetterTextFieldWidget( 998, this.textRenderer, searchBoxX, 22, searchBoxWidth, 20 );
		this.searchBox.setListener( new PagedEntryListWidget.Listener() {
			@Override
			public void setBooleanValue( int id, boolean value ) {
			}

			@Override
			public void setFloatValue( int id, float value ) {
			}

			@Override
			public void setStringValue( int id, String text ) {
				ModsScreen.this.modList.filter( text, false );
			}
		} );
		this.modList = new ModListWidget(
			this.client,
			this.paneWidth,
			this.height,
			this.paneY + 19,
			this.height - 36,
			ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36,
			this.searchBox.getText(),
			this
		);
		this.modList.setXPos( 0 );
		modList.reloadFilters();

		for ( Mod mod : ModMenu.MODS.values() ) {
			if ( !modHasConfigScreen.containsKey( mod.getId() ) ) {
				try {
					Screen configScreen = ModMenu.getConfigScreen( mod.getId(), this );
					modHasConfigScreen.put( mod.getId(), configScreen != null );
				} catch ( NoClassDefFoundError e ) {
					LOGGER.warn( "The '" + mod.getId() + "' mod config screen is not available because " + e.getLocalizedMessage() + " is missing." );
					modHasConfigScreen.put( mod.getId(), false );
				} catch ( Throwable e ) {
					LOGGER.error( "Error from mod '" + mod.getId() + "'", e );
					modHasConfigScreen.put( mod.getId(), false );
				}
			}
		}

		this.descriptionListWidget = new DescriptionListWidget(
			this.client,
			paneWidth,
			this.height,
			paneY + 60,
			this.height - 36,
			textRenderer.fontHeight + 1,
			this
		);
		this.descriptionListWidget.setXPos( rightPaneX );
		ButtonWidget configureButton = new ModMenuTexturedButtonWidget(
			999,
			width - 24,
			paneY,
			20,
			20,
			0,
			0,
			CONFIGURE_BUTTON_LOCATION,
			32,
			64,
			"",
			button -> {
				final String modid = Objects.requireNonNull( selected ).getMod().getId();
				if ( modHasConfigScreen.get( modid ) ) {
					Screen configScreen = ModMenu.getConfigScreen( modid, this );
					client.setScreen( configScreen );
				} else {
					button.active = false;
				}
			},
			( AbstractButtonWidget button, int mouseX, int mouseY ) -> this.setTooltip( CONFIGURE )
		) {
			@Override
			public void render( MinecraftClient client, int mouseX, int mouseY ) {
				if ( selected != null ) {
					String modid = selected.getMod().getId();
					active = modHasConfigScreen.get( modid );
				} else
					active = false;
				visible = active;
				if ( visible )
					super.render( client, mouseX, mouseY );
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min( urlButtonWidths, 200 );
		ButtonWidget websiteButton = new AbstractButtonWidget(
			996,
			rightPaneX + ( urlButtonWidths / 2 ) - ( cappedButtonWidth / 2 ),
			paneY + 36,
			Math.min( urlButtonWidths, 200 ),
			20,
			new TranslatableText( "modmenu.website" ),
			button -> openLink( Mod::getWebsite )
		) {
			@Override
			public void render( MinecraftClient minecraftClient, int mouseX, int mouseY ) {
				visible = selected != null;
				active = visible && selected.getMod().getWebsite() != null;
				super.render( minecraftClient, mouseX, mouseY );
			}
		};
		ButtonWidget issuesButton = new AbstractButtonWidget(
			995,
			rightPaneX + urlButtonWidths + 4 + ( urlButtonWidths / 2 ) - ( cappedButtonWidth / 2 ),
			paneY + 36,
			Math.min( urlButtonWidths, 200 ),
			20,
			new TranslatableText( "modmenu.issues" ),
			button -> openLink( Mod::getIssueTracker )
		) {

			@Override
			public void render( MinecraftClient client, int mouseX, int mouseY ) {
				visible = selected != null;
				active = visible && selected.getMod().getIssueTracker() != null;
				super.render( client, mouseX, mouseY );
			}
		};
		this.addChild( this.modList );
		this.addChild( this.searchBox );
		this.buttons.add( new ModMenuTexturedButtonWidget(
			994,
			paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2,
			22,
			20,
			20,
			0,
			0,
			FILTERS_BUTTON_LOCATION,
			32,
			64,
			"",
			button -> filterOptionsShown = !filterOptionsShown,
			( AbstractButtonWidget button, int mouseX, int mouseY ) -> this.setTooltip( TOGGLE_FILTER_OPTIONS )
		) );
		Text showLibrariesText = ModMenuConfig.SHOW_LIBRARIES.getButtonText();
		Text sortingText = ModMenuConfig.SORTING.getButtonText();
		int showLibrariesWidth = textRenderer.getStringWidth( showLibrariesText.asFormattedString() ) + 20;
		int sortingWidth = textRenderer.getStringWidth( sortingText.asFormattedString() ) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.buttons.add( new AbstractButtonWidget(
			994,
			filtersX,
			45,
			sortingWidth,
			20,
			sortingText,
			button -> {
				ModMenuConfig.SORTING.cycleValue();
				ModMenuConfigManager.save();
				modList.reloadFilters();
			}
		) {

			@Override
			public void render( MinecraftClient minecraftClient, int mouseX, int mouseY ) {
				GlStateManager.translate( 0, 0, 1 );
				visible = filterOptionsShown;
				String name = I18n.translate( "option." + ModMenu.MOD_ID + "." + ModMenuConfig.SORTING.getKey() );
				String value = I18n.translate(
					"option." + ModMenu.MOD_ID + "." +
						ModMenuConfig.SORTING.getKey() +
						"." + ModMenuConfig.SORTING.getValue().toString().toLowerCase( Locale.ROOT )
				);
				this.setMessage( new LiteralText( name + ": " + value ) );
				super.render( minecraftClient, mouseX, mouseY );
			}
		} );
		this.buttons.add( new AbstractButtonWidget(
			993,
			filtersX + sortingWidth + 2,
			45,
			showLibrariesWidth,
			20,
			showLibrariesText,
			button -> {
				ModMenuConfig.SHOW_LIBRARIES.toggleValue();
				ModMenuConfigManager.save();
				modList.reloadFilters();
			}
		) {
			@Override
			public void render( MinecraftClient minecraftClient, int mouseX, int mouseY ) {
				GlStateManager.translate( 0, 0, 1 );
				visible = filterOptionsShown;
				String name = I18n.translate( "option." + ModMenu.MOD_ID + "." + ModMenuConfig.SHOW_LIBRARIES.getKey() );
				String value = I18n.translate(
					"option." + ModMenu.MOD_ID + "." +
						ModMenuConfig.SHOW_LIBRARIES.getKey() +
						"." + Boolean.toString( ModMenuConfig.SHOW_LIBRARIES.getValue() ).toLowerCase( Locale.ROOT )
				);
				this.setMessage( new LiteralText( name + ": " + value ) );
				super.render( minecraftClient, mouseX, mouseY );
			}
		} );
		if ( !ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue() ) {
			this.buttons.add( configureButton );
		}
		this.buttons.add( websiteButton );
		this.buttons.add( issuesButton );
		this.addChild( this.descriptionListWidget );
		this.buttons.add( new AbstractButtonWidget(
			992,
			this.width / 2 - 154,
			this.height - 28,
			150,
			20,
			new TranslatableText( "modmenu.modsFolder" ),
			button -> UrlUtil.getOperatingSystem().open( new File( FabricLoader.getInstance().getGameDir().toFile(), "mods" ) )
		) );
		this.buttons.add( new AbstractButtonWidget(
			993,
			this.width / 2 + 4,
			this.height - 28,
			150,
			20,
			ScreenTexts.DONE,
			button -> client.setScreen( this.getPreviousScreen() )
		) );

		init = true;
	}

	@Override
	public void keyPressed( char chr, int keyCode ) {
		this.searchBox.keyPressed( chr, keyCode );
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int button ) {
		super.mouseClicked( mouseX, mouseY, button );
		this.modList.mouseClicked( mouseX, mouseY, button );
		this.searchBox.mouseClicked( mouseX, mouseY, button );
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {
		this.renderBackground();
		this.tooltip = null;
		ModListEntry selectedEntry = selected;
		super.renderChildren( mouseX, mouseY, delta );
		if ( selectedEntry != null ) {
			this.descriptionListWidget.render( mouseX, mouseY, delta );
		}
		this.renderLabels( mouseX, mouseY );
		this.renderButtons( mouseX, mouseY, delta );
//		textRenderer.draw(  // very useful debug text
//			String.format( "X: %s Y %s OnEntry: %s", mouseX, mouseY, this.modList.getEntryAt( mouseX, mouseY ) ),
//			10, 10, 0xF0F0F0
//		);

		GlStateManager.disableBlend();
		drawCenteredString( this.textRenderer, I18n.translate( "modmenu.title" ), this.modList.getWidth() / 2, 8, 16777215 );
		Text fullModCount = computeModCountText( true );
		if ( updateFiltersX() ) {
			if ( filterOptionsShown ) {
				if ( !ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getStringWidth( fullModCount.asFormattedString() ) <= filtersX - 5 ) {
					textRenderer.draw( fullModCount.asFormattedString(), searchBoxX, 52, 0xFFFFFF );
				} else {
					textRenderer.draw( computeModCountText( false ).asFormattedString(), searchBoxX, 46, 0xFFFFFF );
					textRenderer.draw( computeLibraryCountText().asFormattedString(), searchBoxX, 57, 0xFFFFFF );
				}
			} else {
				if ( !ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getStringWidth( fullModCount.asFormattedString() ) <= modList.getWidth() - 5 ) {
					textRenderer.draw( fullModCount.asFormattedString(), searchBoxX, 52, 0xFFFFFF );
				} else {
					textRenderer.draw( computeModCountText( false ).asFormattedString(), searchBoxX, 46, 0xFFFFFF );
					textRenderer.draw( computeLibraryCountText().asFormattedString(), searchBoxX, 57, 0xFFFFFF );
				}
			}
		}
		if ( selectedEntry != null ) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ( "java".equals( mod.getId() ) ) {
				DrawingUtil.drawRandomVersionBackground( mod, x, paneY, 32, 32 );
			}
			this.selected.bindIconTexture();
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
			GlStateManager.enableBlend();
			drawTexture( x, paneY, 0.0F, 0.0F, 32, 32, 32, 32 );
			GlStateManager.disableBlend();
			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			String name = mod.getName();
			String trimmedName = name;
			int maxNameWidth = this.width - ( x + imageOffset );
			if ( textRenderer.getStringWidth( name ) > maxNameWidth ) {
				String ellipsis = "...";
				trimmedName = textRenderer.trimToWidth( name, maxNameWidth - textRenderer.getStringWidth( ellipsis ) ) + ellipsis;
			}
			textRenderer.draw( trimmedName, x + imageOffset, paneY + 1, 0xFFFFFF );
			if ( mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + textRenderer.fontHeight && mouseX < x + imageOffset + textRenderer.getStringWidth( trimmedName ) ) {
				setTooltip( new TranslatableText( "modmenu.modIdToolTip", mod.getId() ) );
			}
			if ( init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod ) {
				modBadgeRenderer = new ModBadgeRenderer( x + imageOffset + this.client.textRenderer.getStringWidth( trimmedName ) + 2, paneY, width - 28, selectedEntry.getMod(), this );
				init = false;
			}
			if ( !ModMenuConfig.HIDE_BADGES.getValue() ) {
				modBadgeRenderer.draw( mouseX, mouseY );
			}
			if ( mod.isReal() ) {
				textRenderer.draw( mod.getPrefixedVersion(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080 );
			}
			String authors;
			List<String> names = mod.getAuthors();

			if ( !names.isEmpty() ) {
				if ( names.size() > 1 ) {
					authors = Joiner.on( ", " ).join( names );
				} else {
					authors = names.get( 0 );
				}
				DrawingUtil.drawWrappedString(
					I18n.translate( "modmenu.authorPrefix", authors ),
					x + imageOffset,
					paneY + 2 + lineSpacing * 2,
					paneWidth - imageOffset - 4,
					1,
					0x808080
				);
			}
		}
		if ( this.tooltip != null ) {
			this.renderTooltip(
				textRenderer.wrapLines( this.tooltip.asFormattedString(), Integer.MAX_VALUE ),
				mouseX,
				mouseY
			);
		}
	}

	private Text computeModCountText( boolean includeLibs ) {
		int[] rootMods = formatModCount( ModMenu.ROOT_MODS.values().stream().filter( mod -> !mod.getBadges().contains( Mod.Badge.LIBRARY ) ).map( Mod::getId ).collect( Collectors.toSet() ) );

		if ( includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue() ) {
			int[] rootLibs = formatModCount( ModMenu.ROOT_MODS.values().stream().filter( mod -> mod.getBadges().contains( Mod.Badge.LIBRARY ) ).map( Mod::getId ).collect( Collectors.toSet() ) );
			return TranslationUtil.translateNumeric( "modmenu.showingModsLibraries", rootMods, rootLibs );
		} else {
			return TranslationUtil.translateNumeric( "modmenu.showingMods", rootMods );
		}
	}

	private Text computeLibraryCountText() {
		if ( ModMenuConfig.SHOW_LIBRARIES.getValue() ) {
			int[] rootLibs = formatModCount( ModMenu.ROOT_MODS.values().stream().filter( mod -> mod.getBadges().contains( Mod.Badge.LIBRARY ) ).map( Mod::getId ).collect( Collectors.toSet() ) );
			return TranslationUtil.translateNumeric( "modmenu.showingLibraries", rootLibs );
		} else {
			return new LiteralText( null );
		}
	}

	private int[] formatModCount( Set<String> set ) {
		int visible = modList.getDisplayedCountFor( set );
		int total = set.size();
		if ( visible == total )
			return new int[] { total };
		return new int[] { visible, total };
	}

	@Override
	public void renderBackground() {
		ModsScreen.overlayBackground( 0, 0, this.width, this.height, (int) this.scrollPercent );
	}

	public static void overlayBackground( int x1, int y1, int x2, int y2, int scrollAmount ) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		MinecraftClient.getInstance().getTextureManager().bindTexture( DrawableHelper.OPTIONS_BACKGROUND_TEXTURE );
		GlStateManager.color( 0.0F, 0.0F, 0.0F, 1.0F );
		bufferBuilder.begin( 7, VertexFormats.POSITION_TEXTURE_COLOR );
		bufferBuilder.vertex( x1, y2, 0.0D ).texture( x1 / 32.0F, (float) ( y2 + scrollAmount ) / 32.0F ).color( 64, 64, 64, 255 ).next();
		bufferBuilder.vertex( x2, y2, 0.0D ).texture( x2 / 32.0F, (float) ( y2 + scrollAmount ) / 32.0F ).color( 64, 64, 64, 255 ).next();
		bufferBuilder.vertex( x2, y1, 0.0D ).texture( x2 / 32.0F, (float) ( y1 + scrollAmount ) / 32.0F ).color( 64, 64, 64, 255 ).next();
		bufferBuilder.vertex( x1, y1, 0.0D ).texture( x1 / 32.0F, (float) ( y1 + scrollAmount ) / 32.0F ).color( 64, 64, 64, 255 ).next();
		tessellator.draw();
	}

	private void setTooltip( Text tooltip ) {
		this.tooltip = tooltip;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry( ModListEntry entry ) {
		if ( entry != null ) {
			this.selected = entry;
		}
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent( double scrollPercent ) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getText();
	}

	private boolean updateFiltersX() {
		if (
			( filtersWidth + textRenderer.getStringWidth( computeModCountText( true ).asFormattedString() ) + 20 ) >= searchRowWidth &&
				( ( filtersWidth + textRenderer.getStringWidth( computeModCountText( false ).asFormattedString() ) + 20 ) >= searchRowWidth ||
					( filtersWidth + textRenderer.getStringWidth( computeLibraryCountText().asFormattedString() ) + 20 ) >= searchRowWidth )
		) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return modHasConfigScreen;
	}

	public void openLink( Function<Mod, String> linkProducer ) {
		final Mod mod = Objects.requireNonNull( selected ).getMod();
		this.client.setScreen( new ConfirmChatLinkScreen(
			( bool, id ) -> {
				if ( bool )
					UrlUtil.getOperatingSystem().open( linkProducer.apply( mod ) );
				this.client.setScreen( this );
			},
			linkProducer.apply( mod ),
			999,
			false
		) );
	}
}
