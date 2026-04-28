package core.game.ui;

import core.board.Board;
import core.game.ui.connect6.BeautyGUI;
import core.player.Player;
import jagoclient.Global;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class UiFactory {
	
	static {

    	Global.setApplet(false);
		Global.home(System.getProperty("user.home"));
		Global.readparameter(".go.cfg");
		Global.createfonts();
	}

	public static GameUI getUi(String type, String title) {

		try {
			Class uiClass = Class.forName(Configuration.GUI_TYPE);
			Constructor constructor = uiClass.getConstructor(String.class);
			return (GameUI) constructor.newInstance(title);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
