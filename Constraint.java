package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This class is specifically for position based constraints.
 * It has a default solve method defined, but should be overloaded for custom constraints defined
 * 	by inherited classes.
 * The default method assumes an ineqaulity constraint exists to solve.
 */

public class Constraint {
	//Fields for the incident and reference positions (2 for reference as reference will likely suggest a line segment)
	protected Vec2 inc;
	protected Vec2 refA, refB;
	//Mass for the positions
	protected double incMass;
	protected double refMass;
	
	//Constructor in case no second reference point is needed
	public Constraint(Vec2 incident, Vec2 reference) {
		inc = incident;
		refA = reference;
		refB = null;//Ensure this is null for the solve method
		//Assumed reference has infinite mass
		incMass = 1.0;
		refMass = 0.0;
	}
	
	//Constructor in case the incident positions intersection a line segment
	public Constraint(Vec2 incident, Vec2 referencePointA, Vec2 referencePointB) {
		inc = incident;
		refA = referencePointA;
		refB = referencePointB;
		//Assumed reference has infinite mass
		incMass = 1.0;
		refMass = 0.0;
	}
	
	//Solves an intersection inequality problem.
	public void solve() {
		//This needs to be overloaded by a child Class
		//By default, assume a point to line projection, which is essentially an intersection violation
		//	solve.
		Vec2 norm;
		if (refB != null) {
			//Position to Line constraint to solve
			norm = Vec2.perpendicularNormal(refA, refB);
			//Commenting out the below -- this will need a more thorough analysis, such by
			//	adding the reference objects center (origin) to determine which side of a line is the
			//	most appropriate normal vector for separation.
			/*if (Vec2.dot(norm, inc) > 0) {
				//Flip signs to ensure the normal is the direction the reference object needs to move along.
				norm.x *= -1.0;
				norm.y *= -1.0;
			}*/
			//Get the violation from the incident point to the reference line (just a point to plane distance problem).
			double violation = Vec2.dot(norm, new Vec2(inc.x - refA.x, inc.y - refA.y));// - 5.0;
			double jWeight = incMass + refMass;
			double j = incMass / jWeight;
			//Not worrying about solving this a linear complementary problem, just enforcing positive change
			if (violation <= 0.03)return;
			inc.x -= (violation * norm.x) * j;
			inc.y -= (violation * norm.y) * j;
			if (refMass != 0.0) {
				//Ignore the reference positions if it has infinite mass
				j = -refMass / jWeight;//negated because the normal is currently representing the solve direction for inc not ref
				//need to distribute the solve between the two positions, check via barycentric coordinates
				//Not implementing here, only worried about getting the basic dynamic versus static object test working against gravity.
			}
			
		} else {
			//Position to Position constraint to solve
		}
	}
}
