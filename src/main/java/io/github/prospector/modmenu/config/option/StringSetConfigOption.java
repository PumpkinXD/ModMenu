package io.github.prospector.modmenu.config.option;

import io.github.prospector.modmenu.util.TranslationUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Set;

public class StringSetConfigOption {
	private final String key, translationKey;
	private final Set<String> defaultValue;

	public StringSetConfigOption( String key, Set<String> defaultValue ) {
		super();
		ConfigOptionStorage.setStringSet( key, defaultValue );
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf( "option", key );
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public Set<String> getValue() {
		return ConfigOptionStorage.getStringSet( key );
	}

	public void setValue( Set<String> value ) {
		ConfigOptionStorage.setStringSet( key, value );
	}

	public Text getMessage() {
		return new TranslatableText( translationKey );
	}

	public Set<String> getDefaultValue() {
		return defaultValue;
	}
}
