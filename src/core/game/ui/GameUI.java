package core.game.ui;

import java.util.Observer;

import core.game.timer.GameTimer;

public interface GameUI extends Observer{
	//在UI上放置可视化的计时器，当前框架没有用到。
	public void setTimer(GameTimer bTimer, GameTimer wTimer);
}
