package core.game.ui.connect6;

import java.awt.Frame;
import java.util.Observable;

import core.game.timer.GameTimer;
import core.game.ui.GameUI;
import jagoclient.board.GoFrame;
//六子棋的一种GUI
public class BeautyGUI implements GameUI {
	
	GoFrame frame = null;
	
	public BeautyGUI(String title) {
		frame = new GoFrame(new Frame(), title);
	}

	@Override
	public void update(Observable o, Object arg) {

		char move[] = arg.toString().toCharArray();
		
		int i = move[0] - 'A';
    	int j = move[1] - 'A';	    	
    	frame.B.set(j, i);
    	frame.B.showinformation();
    	frame.B.copy();
    	
    	i = move[2] - 'A';
    	j = move[3] - 'A';
    	frame.B.set(j, i);
    	frame.B.showinformation();
    	frame.B.copy();
	}

	@Override
	public void setTimer(GameTimer bTimer, GameTimer wTimer) {
		//在GoFrame上设置具有GUI的timer.
		// To do
	}
}
