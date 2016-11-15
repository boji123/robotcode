package test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * ��ͼ�࣬���ڱ������е����Լ����Լ���λ����Ϣ
 * 
 * @author baiji
 *
 */
public class BattleMap {
	RobotInfo yourself = new RobotInfo();
	Hashtable enemyList = new Hashtable<>();
	final double PI=Math.PI;
	RobotInfo nowEnemy=new RobotInfo();
	private int midpointcount=0;
	private double midpointstrength=0;
	// robotinfo:
	// String name;
	// double locationX;
	// double locationY;
	// double bearing;
	// double distance;
	// double heading;
	// double velocity;
	// double energy;

	
	/**
	 * �������Լ��ڵ�ͼ�ϵ���Ϣ���ú�������ѭ�������Ե���
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.setLocationX(you.getX());
		yourself.setLocationY(you.getY());
		yourself.setHeading(you.getHeading());
		yourself.setSpeed(you.getVelocity());
		yourself.setSelfFlag(1);
	}

	/**
	 * ���õ��˵���Ϣ��֧�ֶ������
	 */
	public void setEnemyInfo(ScannedRobotEvent e,AdvancedRobot me) {
		RobotInfo enemy;
		if(enemyList.containsKey(e.getName())){
			enemy=(RobotInfo)enemyList.get(e.getName());
		}else {
			enemy=new RobotInfo();
			enemyList.put(e.getName(), enemy);
		}
		enemy.setSelfFlag(0);
		double absbearing_rad = (me.getHeadingRadians()+e.getBearingRadians())%(2*PI);
		double h = FirstMove.normalizeBearing(e.getHeadingRadians() - enemy.getHeading());
		if((me.getTime() - enemy.getCtime())!=0)		
			h = h/(me.getTime() - enemy.getCtime());
		enemy.setChangehead(h);
		// �˴�Ӧ���������Ե�λ�ø��µ��˵ķ�λ;
		enemy.setLocationX(me.getX()+Math.sin(absbearing_rad)*e.getDistance()); //works out the x coordinate of where the target is
		enemy.setLocationY(me.getY()+Math.cos(absbearing_rad)*e.getDistance());  //works out the y coordinate of where the target is
		enemy.setCtime(me.getTime());//�˿̵���Ϸʱ��	
		enemy.setAlive(true);
		enemy.setName(e.getName());
		enemy.setBearing(e.getBearingRadians());
		enemy.setDistance(e.getDistance());
		enemy.setHeading(e.getHeadingRadians());
		enemy.setSpeed(e.getVelocity());
		enemy.setFifo(me);
		if ((enemy.getFifo()<nowEnemy.getFifo())||(nowEnemy.isAlive()== false)) {
			nowEnemy=enemy;
		}
	}

	/**
	 * �������������ӵ�ͼ���Ƴ�
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		RobotInfo deadRobot=(RobotInfo)enemyList.get(robotName);
		deadRobot.setAlive(false);
		enemyList.remove(deadRobot);
	}

	/**
	 * ���ݵ�ͼ��������������һ���ڹ��˶�
	 */
	public NextAimInfo calcuNextGunBearing() {
		NextAimInfo nextAimInfo = new NextAimInfo();
		nextAimInfo.setBearing(50);
		nextAimInfo.setIfCanFire(true);
		return nextAimInfo;
	}

	/**
	 * ���ݵ�ͼ��������������һ�������˶�
	 */
	public NextMoveInfo calcuNextMove(AdvancedRobot me) {
		NextMoveInfo nextMoveInfo = new NextMoveInfo();
		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravPoint p;
		RobotInfo en;
    	Enumeration e = enemyList.elements();
		while (e.hasMoreElements()) {
    	    en = (RobotInfo)e.nextElement();
    	    if(en.getSelfFlag()==0){
    	    	if (en.isAlive()) {
    	    		p = new GravPoint(en.getLocationX(),en.getLocationY(), -1000);
    	    		force = p.power/Math.pow(getRange(me.getX(),me.getY(),p.x,p.y),2);
    	    		ang = FirstMove.normalizeBearing(Math.PI/2 - Math.atan2(me.getY() - p.y, me.getX() - p.x)); 
    	    		xforce += Math.sin(ang) * force;
    	    		yforce += Math.cos(ang) * force;
    	    	}
    	    }
	    }
		
		//ǽ��Ӱ��
		double[] xrange={0,me.getBattleFieldWidth()};
		double[] yrange={0,me.getBattleFieldHeight()};
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				p = new GravPoint(xrange[i],yrange[j], -1000);
	    		force = p.power/Math.pow(getRange(me.getX(),me.getY(),p.x,p.y),2);
	    		ang = FirstMove.normalizeBearing(Math.PI/2 - Math.atan2(me.getY() - p.y, me.getX() - p.x)); 
	    		xforce += Math.sin(ang) * force;
	    		yforce += Math.cos(ang) * force;
			}
		}
		
		
	    
		//�ο���˵��Ч���ã����忴�����������޸Ļ��ߵ����ⲿ��
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		/*
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new GravPoint(me.getBattleFieldWidth()/2, me.getBattleFieldHeight()/2, midpointstrength);
		force = p.power/Math.pow(getRange(me.getX(),me.getY(),p.x,p.y),1.5);
	    ang = FirstMove.normalizeBearing(Math.PI/2 - Math.atan2(me.getY() - p.y, me.getX() - p.x)); 
	    xforce += Math.sin(ang) * force;
	    yforce += Math.cos(ang) * force;
	   
	    */
	    //ǽ��Ӱ��
	    xforce += 5000/Math.pow(getRange(me.getX(), me.getY(), me.getBattleFieldWidth(), me.getY()), 3);
	    xforce -= 5000/Math.pow(getRange(me.getX(), me.getY(), 0, me.getY()), 3);
	    yforce += 5000/Math.pow(getRange(me.getX(), me.getY(), me.getX(), me.getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(getRange(me.getX(), me.getY(), me.getX(), 0), 3);
	    
	    //Move in the direction of our resolved force.
	    nextMoveInfo=nextMoveInfo.goTo(me.getX()-xforce,me.getY()-yforce,me);
		return nextMoveInfo;
	}

	public static double getRange( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}
	
	//�����Ľṹ��
	class GravPoint {
	    public double x,y,power;
	    public GravPoint(double pX,double pY,double pPower) {
	        x = pX;
	        y = pY;
	        power = pPower;
	    }
	}
}