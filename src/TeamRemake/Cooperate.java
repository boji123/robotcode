package TeamRemake;

import java.io.IOException;

import robocode.MessageEvent;
import robocode.TeamRobot;

public class Cooperate {
	TeamRobot battle;
	BattleMap battleMap;
	static int teammates = 2;

	int teammatesRest = 0;
	boolean isLeader;
	Force cornerForce = new Force();// 0左下1左上2右上3右下

	public void init(TeamRobot battle, BattleMap battleMap) {
		this.battle = battle;
		this.battleMap = battleMap;
		if (battle.getEnergy() > 150)
			isLeader = true;
		else
			isLeader = false;

		if (battle.getTeammates() == null)
			teammatesRest = 0;
		else
			teammatesRest = battle.getTeammates().length;
	}

	public int getEnemyRest() {
		return battle.getOthers() - teammatesRest;
	}

	public void divideCornerForTeam() {// 仅仅针对两个队友的策略
		if (teammates == 2 && isLeader) {
			RobotInfo teammate1 = battleMap.getRobot(battle.getTeammates()[0]);
			RobotInfo teammate2 = battleMap.getRobot(battle.getTeammates()[1]);

			try {
				battle.sendMessage(teammate1.getName(), "corner:1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				battle.sendMessage(teammate2.getName(), "corner:2");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cornerForce.xForce = -2;
			cornerForce.yForce = -2;

		}
	}

	public void onMessageReceived(MessageEvent event) {
		String receive = event.getMessage().toString();
		System.out.println(receive);

		if (receive.substring(0, 6).compareTo("corner") == 0) {
			String str = receive.substring(7, 8);

			if (str.compareTo("1") == 0) {
				System.out.println("receiveStr:" + str);
				cornerForce.xForce = 2;
				cornerForce.yForce = -2;
			}
			if (str.compareTo("2") == 0) {
				System.out.println("receiveStr:" + str);
				cornerForce.xForce = 2;
				cornerForce.yForce = 2;
			}
		}
	}

	public boolean ifReachPlace() {
		return true;
	}
}
