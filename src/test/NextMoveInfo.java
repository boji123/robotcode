package test;

import robocode.AdvancedRobot;

/**
 * 数据结构，下一步移动的量，包括方向与距离
 * 
 * @author baiji
 *
 */
public class NextMoveInfo {
	private double distance;// 移动的距离
	private double bearing;// 转弯的朝向
	
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getBearing() {
		return bearing;
	}
	public void setBearing(double bearing) {
		this.bearing = bearing;
	}
	
	public NextMoveInfo goTo(double x, double y,AdvancedRobot me) {
	    double dist = 20; 
	    double angle = Math.toDegrees(absbearing(me.getX(),me.getY(),x,y));
	    double r = turnTo(angle,me);
	    this.setDistance(dist * r);
	    return this;
	}
	public double turnTo(double angle,AdvancedRobot me) {
	    double ang;
    	double dir;
	    ang = FirstMove.normalizeBearing((me.getHeading() - angle));
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    }
	    else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    }
	    else {
	        dir = 1;
	    }
	    setBearing(ang);
	    return dir;
	}
	public double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = BattleMap.getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 )
		{
			return Math.asin( xo / h );
		}
		if( xo > 0 && yo < 0 )
		{
			return Math.PI - Math.asin( xo / h );
		}
		if( xo < 0 && yo < 0 )
		{
			return Math.PI + Math.asin( -xo / h );
		}
		if( xo < 0 && yo > 0 )
		{
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		return 0;
	}
}
