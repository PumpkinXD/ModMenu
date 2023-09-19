package io.github.prospector.modmenu.config.option;

import io.github.prospector.modmenu.gui.widget.AbstractButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class Option {
	private final Text key;

	public Option( String key ) {
		this.key = new TranslatableText( key );
	}

	public abstract AbstractButtonWidget createButton( GameOptions options, int id, int x, int y, int width );

	protected Text getDisplayPrefix() {
		return this.key;
	}

	protected Text getPixelLabel( int pixel ) {
		return new TranslatableText( "options.pixel_value", this.getDisplayPrefix(), pixel );
	}

	protected Text getPercentLabel( double proportion ) {
		return new TranslatableText( "options.percent_value", this.getDisplayPrefix(), (int) ( proportion * 100.0 ) );
	}

	protected Text getPercentAdditionLabel( int percentage ) {
		return new TranslatableText( "options.percent_add_value", this.getDisplayPrefix(), percentage );
	}

	protected Text getGenericLabel( Text value ) {
		return new TranslatableText( "options.generic_value", this.getDisplayPrefix(), value );
	}

	protected Text getGenericLabel( int value ) {
		return this.getGenericLabel( new LiteralText( Integer.toString( value ) ) );
	}
}
