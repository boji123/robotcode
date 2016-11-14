package test;

import robocode.*;

/**
 * 
 * @author baiji
 *
 */
public class FirstMove extends AdvancedRobot {
	BattleMap map = new BattleMap();

	public void run() {
		// ����������������ֶ�������
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while (true) {
			map.setYourInfo(this);
			setScan();
			setMove();
			setFire();
			execute();
		}
	}

	public void setScan() {
		if (getRadarTurnRemaining() == 0) {
			// û���¼�����ʱ����ͨ���Ʒ�ʽ
			setTurnRadarLeft(360);
		}
	}

	public void setMove() {
		if (getDistanceRemaining() <= 20 && getTurnRemaining() <= 30) {// �����һ���˶�����ִ���꣬ǰ��20������ת��30��ʱ����
			// û���¼�����ʱ����ͨ���Ʒ�ʽ
			NextMoveInfo nextMoveInfo = map.calcuNextMove();
			setTurnRight(nextMoveInfo.bearing);
			setAhead(nextMoveInfo.distance);
		}
	}

	public void setFire() {
		if (getGunTurnRemaining() <= 30) {// �����һ����׼����ִ����
			// û���¼�����ʱ����ͨ���Ʒ�ʽ
			NextAimInfo nextAimInfo = map.calcuNextGunBearing();
			setTurnGunRight(nextAimInfo.bearing);
			if (nextAimInfo.ifCanFire) {
				setFire(1);
			}
		}
	}

	// �¼�����
	/**
	 * �״�ɨ�赽һ�����ˣ����Ի�ȡ�����˵���Ϣ
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		map.setEnemyInfo(e);
	}

	/**
	 * ��������
	 */
	public void onRobotDeath(RobotDeathEvent event) {
		map.removeEnemyFromMap(event);
	}

	/**
	 * ײ�������ˣ����Ի�ȡ����bearing�����˷�λ������Ϣ
	 */
	public void onHitRobot(HitRobotEvent event) {
		// event.get___
	}

	/**
	 * ���ӵ����У����Ի�����ӵ�������ķ���
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		// event.get___
	}

	// һЩ��ѧ����
	/**
	 * ������ֹ���꣬���ؽǶȵķ�����ֵ�����Բ�ʹ�øú���ֱ�ӵ���atan2
	 */
	private static double getAngle(double x1, double y1, double x2, double y2) {
		return Math.atan2(x2 - x1, y2 - y1);
	}

	/**
	 * ����-2pi~2pi����ĽǶ�ֵ������-pi��pi�Ĺ淶���Ƕȣ�����ϵͳʹ�ã�
	 */
	private static double normalizeBearing(double angle) {
		if (angle < -Math.PI)
			angle += 2 * Math.PI;
		if (angle > Math.PI)
			angle -= 2 * Math.PI;
		return angle;
	}

	// ------------------------------------------------------------------------------------
	/**
	 * �����ʤ���Ļ�����ִ����һ��װ����
	 */
	public void onWin(WinEvent e) {
		for (int i = 0; i < 50; i++) {

			turnGunRightRadians(Math.PI * 3 / 4);
			turnGunLeftRadians(Math.PI * 3 / 4);
		}
	}
}
