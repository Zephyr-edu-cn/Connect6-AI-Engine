package core.game.timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
//通用的Gametimer，没有所对应的GUI
public class GameTimer extends Observable implements ActionListener{
	
	private int timeLimit = 0;
	javax.swing.Timer timer;
	
	public GameTimer(int timeLimit) {
		this.timeLimit = timeLimit;
		timer = new javax.swing.Timer(1000, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		timeLimit--;
		setChanged();
		notifyObservers("R");
		//如果超时，
		if (timeLimit <= 0) {
			//通知监听者
			setChanged();
			notifyObservers("T");
		}
	}
	
	public void stop(){
        timer.stop();
    }
	
	public void start() {
		timer.start();
	}

}
