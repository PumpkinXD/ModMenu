package io.github.prospector.modmenu.gui.widget;

import io.github.prospector.modmenu.api.Mod;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.ModsScreen;
import io.github.prospector.modmenu.gui.widget.entries.ModListEntry;
import io.github.prospector.modmenu.util.mod.UrlUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.prospector.modmenu.util.TranslationUtil.hasTranslation;

public class DescriptionListWidget extends BetterEntryListWidget<DescriptionListWidget.DescriptionEntry> {
	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget( MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent ) {
		super( client, width, height, top, bottom, entryHeight );
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + xStart;
	}

	@Override
	protected int getEntryCount() {
		return children().size();
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if ( selectedEntry != lastSelected ) {
			lastSelected = selectedEntry;
			clearEntries();
			scrollAmount = -Float.MAX_VALUE;
			if ( lastSelected != null ) {
				Mod mod = lastSelected.getMod();
				String description = mod.getDescription();
				String translatableDescriptionKey = "modmenu.descriptionTranslation." + mod.getId();
				if ( hasTranslation( translatableDescriptionKey ) ) {
					description = I18n.translate( translatableDescriptionKey );
				}
				if ( !description.isEmpty() ) {
					for ( String line : textRenderer.wrapLines( description.replaceAll( "\n", "\n\n" ), getRowWidth() - 5 ) ) {
						addEntry( new DescriptionEntry( line, this ) );
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ( ( !links.isEmpty() || sourceLink != null ) && !ModMenuConfig.HIDE_MOD_LINKS.getValue() ) {
					addEntry( new DescriptionEntry( "", this ) );
					addEntry( new DescriptionEntry( I18n.translate( "modmenu.links" ), this ) );

					if ( sourceLink != null ) {
						addEntry( new LinkEntry( new LiteralText( "  " ).append( new TranslatableText( "modmenu.source" ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ) ).asFormattedString(), sourceLink, this ) );
					}

					links.forEach( ( key, value ) -> {
						addEntry( new LinkEntry( new LiteralText( "  " ).append( new TranslatableText( key ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ) ).asFormattedString(), value, this ) );
					} );
				}

				Set<String> licenses = mod.getLicense();
				if ( !ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty() ) {
					addEntry( new DescriptionEntry( "", this ) );
					addEntry( new DescriptionEntry( I18n.translate( "modmenu.license" ), this ) );

					for ( String license : licenses ) {
						addEntry( new DescriptionEntry( "  " + license, this ) );
					}
				}

				if ( !ModMenuConfig.HIDE_MOD_CREDITS.getValue() ) {
					if ( "minecraft".equals( mod.getId() ) ) {
						addEntry( new DescriptionEntry( "", this ) );
						addEntry( new MojangCreditsEntry( new TranslatableText( "modmenu.viewCredits" ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ).asFormattedString(), this ) );
					} else if ( "java".equals( mod.getId() ) ) {
						addEntry( new DescriptionEntry( "", this ) );
					} else {
						List<String> authors = mod.getAuthors();
						List<String> contributors = mod.getContributors();
						if ( !authors.isEmpty() || !contributors.isEmpty() ) {
							addEntry( new DescriptionEntry( "", this ) );
							addEntry( new DescriptionEntry( I18n.translate( "modmenu.credits" ), this ) );
							for ( String author : authors ) {
								addEntry( new DescriptionEntry( "  " + author, this ) );
							}
							for ( String contributor : contributors ) {
								addEntry( new DescriptionEntry( "  " + contributor, this ) );
							}
						}
					}
				}
			}
		}
		super.render( mouseX, mouseY, delta );
	}

	@Override
	protected void renderHoleBackground( int y1, int y2, int startAlpha, int endAlpha ) {
		// Awful hack but it makes the background "seamless"
		ModsScreen.overlayBackground( xStart, y1, xEnd, y2, (int) this.scrollAmount );
	}

	@Override
	public DescriptionEntry getEntry( int index ) {
		return children().get( index );
	}

	protected static class DescriptionEntry extends BetterEntryListWidget.Entry<DescriptionEntry> {
		protected String text;

		public DescriptionEntry( String text, DescriptionListWidget parent ) {
			this.parentList = parent;
			this.text = text;
		}

		@Override
		public void render( int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta ) {
			this.render( index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered );
		}

		@Override
		public void render( int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered ) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow( text, x, y, 0xAAAAAA );
		}

		@Override
		public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
			return false;
		}

		@Override
		public void mouseReleased( int index, int mouseX, int mouseY, int button, int x, int y ) { }

		// updatePosition
		@Override
		public void updatePosition( int index, int x, int y ) { }
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry( String text, String link, DescriptionListWidget widget ) {
			super( text, widget );
			this.link = link;
		}

		@Override
		public void render( int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered ) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow( text, x, y, 0x5555ff );
		}

		@Override
		public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
			if ( isMouseOver( mouseX, mouseY ) ) {
				client.setScreen( new ConfirmChatLinkScreen(
					( open, c ) -> {
						if ( open )
							UrlUtil.getOperatingSystem().open( link );
						client.setScreen( parent );
					},
					link,
					9999,
					false
				) );
			}
			return super.mouseClicked( index, mouseX, mouseY, button, x, y );
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry( String text, DescriptionListWidget widget ) {
			super( text, widget );
		}

		@Override
		public boolean mouseClicked( int index, int mouseX, int mouseY, int button, int x, int y ) {
			if ( isMouseOver( mouseX, mouseY ) ) {
				client.setScreen( new CreditsScreen() );
			}
			return super.mouseClicked( index, mouseX, mouseY, button, x, y );
		}
	}
}
