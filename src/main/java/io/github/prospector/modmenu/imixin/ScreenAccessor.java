package io.github.prospector.modmenu.imixin;

import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.List;

public interface ScreenAccessor {
	List<ButtonWidget> modmenu$getButtons();
}
