package io.github.prospector.modmenu.gui;

import com.google.common.collect.ImmutableList;
import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.widget.AbstractButtonWidget;
import io.github.prospector.modmenu.gui.widget.ButtonListWidget;
import io.github.prospector.modmenu.gui.widget.CyclingButtonWidget;
import io.github.prospector.modmenu.util.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModMenuOptionsScreen extends AbstractScreen {
	private ButtonListWidget list;

	public ModMenuOptionsScreen( Screen previous ) {
		super( previous );
	}

	@Override
	public void init() {
		this.list = new ButtonListWidget( this.client, this.width, this.height, 32, this.height - 32, 25 );
		this.list.addAll( ModMenuConfig.asOptions(), 998 );
		this.addChild( this.list );
		this.buttons.add( new AbstractButtonWidget(
			999,
			this.width / 2 - 100,
			this.height - 27,
			200,
			20,
			ScreenTexts.DONE,
			( button ) -> {
				ModMenuConfigManager.save();
				this.client.setScreen( this.getPreviousScreen() );
			}
		) {
		} );
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int button ) {
		this.list.mouseClicked( mouseX, mouseY, button );
		super.mouseClicked( mouseX, mouseY, button );
	}

	@Override
	public void render( int mouseX, int mouseY, float delta ) {
		this.renderBackground();
		super.render( mouseX, mouseY, delta );
		drawCenteredString( this.textRenderer, I18n.translate( "modmenu.options" ), this.width / 2, 5, 0xffffff );
		List<String> list = getHoveredButtonTooltip( this.list, mouseX, mouseY );
		if ( list != null )
			this.renderTooltip( list, mouseX, mouseY );
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
	}

	public static List<String> getHoveredButtonTooltip( ButtonListWidget buttonList, int mouseX, int mouseY ) {
		Optional<AbstractButtonWidget> optional = buttonList.getHoveredButton( mouseX, mouseY );
		return optional.isPresent() && optional.get() instanceof CyclingButtonWidget
			? ( (CyclingButtonWidget<?>) optional.get() ).getTooltip()
			.stream()
			.map( Text::asFormattedString )
			.collect( Collectors.toList() )
			: ImmutableList.of();
	}
}
