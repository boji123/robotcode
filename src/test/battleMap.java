package test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 地图类，用于保存所有敌人以及你自己的位置信息
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
	 * 设置你自己在地图上的信息，该函数由主循环周期性调用
	 */
	public void setYourInfo(AdvancedRobot you) {
		yourself.setLocationX(you.getX());
		yourself.setLocationY(you.getY());
		yourself.setHeading(you.getHeading());
		yourself.setSpeed(you.getVelocity());
		yourself.setSelfFlag(1);
	}

	/**
	 * 设置敌人的信息，支持多个敌人
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
		// 此处应当根据你自的位置更新敌人的方位;
		enemy.setLocationX(me.getX()+Math.sin(absbearing_rad)*e.getDistance()); //works out the x coordinate of where the target is
		enemy.setLocationY(me.getY()+Math.cos(absbearing_rad)*e.getDistance());  //works out the y coordinate of where the target is
		enemy.setCtime(me.getTime());//此刻的游戏时间	
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
	 * 若敌人死亡，从地图上移除
	 */
	public void removeEnemyFromMap(RobotDeathEvent event) {
		String robotName = event.getName();
		RobotInfo deadRobot=(RobotInfo)enemyList.get(robotName);
		deadRobot.setAlive(false);
		enemyList.remove(deadRobot);
	}

	/**
	 * 根据地图的情况返回你的下一步炮管运动
	 */
	public NextAimInfo calcuNextGunBearing() {
		NextAimInfo nextAimInfo = new NextAimInfo();
		nextAimInfo.setBearing(50);
		nextAimInfo.setIfCanFire(true);
		return nextAimInfo;
	}

	/**
	 * 根据地图的情况返回你的下一步车体运动
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
		
		//墙角影响
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
		
		
	    
		//参考他说的效果好，具体看后面测试如何修改或者调整这部分
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
	    //墙的影响
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
	
	//斥力的结构体
	class GravPoint {
	    public double x,y,power;
	    public GravPoint(double pX,double pY,double pPower) {
	        x = pX;
	        y = pY;
	        power = pPower;
	    }
	}
}