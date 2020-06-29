package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This Class builds a constraint that will attempt to ensure a distance between two
 * 	positions are preserved.
 * An assumption that equal weight in the solve is used for these positions -- i.e. points of like mass.
 */

public class DistanceConstraint extends Constraint {
	//The extra value needed for this type of constraint
	private double distance;
	
	//Constructor builds its parent (super) and sets its specialized field.
	DistanceConstraint(Vec2 incident, Vec2 reference, double distance) {
		super(incident, reference);
		this.distance = distance;
	}
	
	//Overriding the constraint solve method to handle a distance constraint rather than a position constraint.
	@Override
	public void solve() {
		Vec2 refToInc = new Vec2(inc.x - refA.x, inc.y - refA.y);
		double curDist = Vec2.dot(refToInc, refToInc);
		Vec2 norm;
		if (curDist == 0.0) {
			//pick an arbitrary normal -- unlikely to run and should be tested for based on an application's use
			norm = new Vec2(0.0, 1.0);
			//Don't find the root of the current distance, it is zero
			double violation = (curDist - distance) * 0.5;
			inc.x += norm.x * violation;
			inc.y += norm.y * violation;
			refA.x -= norm.x * violation;
			refA.y -= norm.y * violation;
		} else {
			norm = Vec2.normalize(refToInc);
			curDist = Math.sqrt(curDist);
			double violation = (curDist - distance) * 0.5;
			//System.out.println("Distance: " + violation + " " + distance + " norm: " + norm.x + " " + norm.y);
			//System.out.println("DV: " + refToInc.x + " " + refToInc.y + " i: " + inc.x + " " + inc.y + " r: " + refA.x + " " + refA.y);
			inc.x -= norm.x * violation;
			inc.y -= norm.y * violation;
			refA.x += norm.x * violation;
			refA.y += norm.y * violation;
		}
		
	}
}
