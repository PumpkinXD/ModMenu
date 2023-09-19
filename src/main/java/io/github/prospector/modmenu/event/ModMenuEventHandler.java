package io.github.prospector.modmenu.event;

import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.gui.ModsScreen;
import io.github.prospector.modmenu.gui.widget.ModMenuButtonWidget;
import io.github.prospector.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import io.github.prospector.modmenu.imixin.ButtonAccessor;
import io.github.prospector.modmenu.util.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModMenuEventHandler {
	private static final Identifier FABRIC_ICON_BUTTON_LOCATION = new Identifier( ModMenu.MOD_ID, "textures/gui/mods_button.png" );

	public static void afterScreenInit( Screen screen ) {
		if ( screen instanceof TitleScreen ) {
			afterTitleScreenInit( screen );
		} else if ( screen instanceof GameMenuScreen ) {
			afterGameMenuScreenInit( screen );
		}
	}

	private static void afterTitleScreenInit( Screen screen ) {
		final List<ButtonWidget> buttons = Screens.getButtons( screen );
		if ( ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() ) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for ( int i = 0; i < buttons.size(); i++ ) {
				ButtonWidget button = buttons.get( i );
				if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC ) {
					if ( button.visible ) {
						shiftButtons( button, modsButtonIndex == -1, spacing );
						if ( modsButtonIndex == -1 ) {
							buttonsY = button.y;
						}
					}
				}
				if ( buttonHasText( button, "menu.online" ) ) {
					if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.REPLACE_REALMS ) {
						buttons.set( i, new ModMenuButtonWidget( 993, button.x, button.y, button.getWidth(), ( (ButtonAccessor) button ).modmenu$getHeight(), ModMenuApi.createModsButtonText(), screen ) );
					} else {
						if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK ) {
							button.setWidth( 98 );
						}
						modsButtonIndex = i + 1;
						if ( button.visible ) {
							buttonsY = button.y;
						}
					}
				}
			}
			if ( modsButtonIndex != -1 ) {
				if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.CLASSIC ) {
					buttons.add( modsButtonIndex, new ModMenuButtonWidget( 994, screen.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText(), screen ) );
				} else if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.SHRINK ) {
					buttons.add( modsButtonIndex, new ModMenuButtonWidget( 995, screen.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText(), screen ) );
				} else if ( ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.ModsButtonStyle.ICON ) {
					buttons.add( modsButtonIndex, new ModMenuTexturedButtonWidget( 996, screen.width / 2 + 104, buttonsY, 20, 20, 0, 0, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> MinecraftClient.getInstance().setScreen( new ModsScreen( screen ) ) ) {
					} );
				}
			}
		}
	}

	private static void afterGameMenuScreenInit( Screen screen ) {
		final List<ButtonWidget> buttons = Screens.getButtons( screen );
		if ( ModMenuConfig.MODIFY_GAME_MENU.getValue() ) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 8;
			ModMenuConfig.ModsButtonStyle style = ModMenuConfig.MODS_BUTTON_STYLE.getValue().forGameMenu();
			for ( int i = 0; i < buttons.size(); i++ ) {
				ButtonWidget button = buttons.get( i );
				if ( style == ModMenuConfig.ModsButtonStyle.CLASSIC ) {
					if ( button.visible ) {
						shiftButtons( button, modsButtonIndex == -1, spacing );
						if ( modsButtonIndex == -1 )
							buttonsY = button.y;
					}
				}
				if ( buttonHasText( button, "menu.reportBugs" ) ) {
					modsButtonIndex = i + 1;
					if ( style == ModMenuConfig.ModsButtonStyle.SHRINK ) {
						buttons.set( i, new ModMenuButtonWidget( 997, button.x, button.y, button.getWidth(), ( (ButtonAccessor) button ).modmenu$getHeight(), ModMenuApi.createModsButtonText(), screen ) );
					} else {
						modsButtonIndex = i + 1;
						if ( button.visible ) {
							buttonsY = button.y;
						}
					}
				}
			}
			if ( modsButtonIndex != -1 ) {
				if ( style == ModMenuConfig.ModsButtonStyle.CLASSIC )
					buttons.add(
						modsButtonIndex,
						new ModMenuButtonWidget(
							998,
							screen.width / 2 - 102,
							buttonsY + spacing,
							204,
							20,
							ModMenuApi.createModsButtonText(),
							screen
						)
					);
				else if ( style == ModMenuConfig.ModsButtonStyle.ICON )
					buttons.add(
						modsButtonIndex,
						new ModMenuTexturedButtonWidget(
							999,
							screen.width / 2 + 4 + 100 + 2,
							screen.height / 4 + 72 - 16,
							20,
							20,
							0,
							0,
							FABRIC_ICON_BUTTON_LOCATION,
							32,
							64,
							button -> MinecraftClient.getInstance().setScreen( new ModsScreen( screen ) ) ) {
						}
					);
			}
		}
	}

	private static boolean buttonHasText( ButtonWidget button, String translationKey ) {
		return button.message.equals( translationKey ) || button.message.equals( I18n.translate( translationKey ) );
	}

	@SuppressWarnings("SameParameterValue")
	private static void shiftButtons( ButtonWidget button, boolean shiftUp, int spacing ) {
		if ( shiftUp )
			button.y -= spacing / 2;
		else
			button.y += spacing - ( spacing / 2 );
	}
}
