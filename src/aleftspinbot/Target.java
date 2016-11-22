package aleftspinbot;

import java.awt.Color;

import robocode.AdvancedRobot;

public class Target extends AdvancedRobot {

	public void run() {
		setColors(Color.red, Color.yellow, Color.yellow);
		System.out.println(getX());
		System.out.println(getY());
		while (true) {
			while (true)
				execute();
		}
	}
}
