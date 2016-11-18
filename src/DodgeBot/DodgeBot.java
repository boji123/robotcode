package DodgeBot;

import robocode.*;

public class DodgeBot extends AdvancedRobot {
	double previousEnergy = 100;
	int movementDirection = 1;
	int gunDirection = 1;

	public void run() {
		setTurnGunRight(99999);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// Stay at right angles to the opponent
		setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

		// If the bot has small energy drop,
		// assume it fired
		double changeInEnergy = previousEnergy - e.getEnergy();
		if (changeInEnergy > 0 && changeInEnergy <= 3) {
			// Dodge!
			movementDirection = -movementDirection;
			setAhead((e.getDistance() / 4 + 25) * movementDirection);
		}
		// When a bot is spotted,
		// sweep the gun and radar
		gunDirection = -gunDirection;
		setTurnGunRight(99999 * gunDirection);

		// Fire directly at target
		fire(2);

		// Track the energy level
		previousEnergy = e.getEnergy();
	}
}