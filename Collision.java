package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This class exists to offer a broad definition for an axis aligned bounding box (AABB).
 * Within in are useful methods for building an AABB and to test for intersection with it
 * 	as well as for a narrow phase collision test of a point in a triangle.
 *This could be extended, but was all that was needed to setup a basic PBD themed simulation.
 */

public class Collision {
	//No invalid values for the doubles in min and max, made public for quick access in
	// classes with a "has a" relationship which may do complicated things to their
	//	fields/attributes.
	public Vec2 min, max;
	
	//This constructor initializes an AABB with zero vectors.
	Collision() {
		min = new Vec2(0.0, 0.0);
		max = new Vec2(0.0, 0.0);
	}
	
	//This constructor sets custom values for the AABB.
	Collision(Vec2 min, Vec2 max) {
		this.min = min;
		this.max = max;
	}
	
	//This method does an intersection test between Two AABB objects.
	static public boolean intersection(Collision a, Collision b) {
		//Separating Axis tests, simple as borders are axis aligned.
		//Otherwise, transforming to one frame of reference and depth
		//	testing between object's basic primitives would need to occur.
		return (a.min.x <= b.max.x && a.max.x >= b.min.x) && 
				(a.min.y <= b.max.y && a.max.y >= b.min.y);
	}
	
	//This method returns whether or not a position exists within a triangle.
	static public boolean pointInTriangle(Vec2 s, Vec2 a, Vec2 b, Vec2 c) {
		Vec2 ab = new Vec2(b.x - a.x, b.y - a.y);
		Vec2 ac = new Vec2(c.x - a.x, c.y - a.y);
		Vec2 as = new Vec2(s.x - a.x, s.y - a.y);
		
		//The odd cross product of 2D vectors, not the assumption of a zero z component for a 3-tuple
		double dividor = ab.x * ac.y - ab.y * ac.x;
		
		//Gather barycentric components between the corners of the triangle to determine if the
		//	point is encapsulated within.
		double v = (as.x * ac.y - as.y * ac.x) / dividor;
		double u = (ab.x * as.y - ab.y * as.x) / dividor;
		double w = 1.0 - v - u;
		
		return (u >= 0.0) && (v >= 0.0) && (w >= 0.0);
		
		//Reference from Wolfram Research's MathWorld formula, but the above is more code readable.
		/*double bxc = Vec2.cross(b, c);
		double u = (Vec2.cross(s, c) - Vec2.cross(a, c)) / bxc;
		double v = (Vec2.cross(s,  b) - Vec2.cross(a,  b)) / bxc;
		return u > 0 && v > 0 && (u + v) < 1.0;*/
	}
	
	//This method modifies the second argument to contain the components of a 2-tuple which
	//	have the smallest value, tested with the components of the first argument.
	static public void minVec2(Vec2 testVec, Vec2 minVec) {
		minVec.x = (minVec.x > testVec.x) ? testVec.x : minVec.x;
		minVec.y = (minVec.y > testVec.y) ? testVec.y : minVec.y;
	}
	
	//This method modifies the second argument to contain the components of a 2-tuple which
	//	have the largest value, tested with the components of the first argument.
	static public void maxVec2(Vec2 testVec, Vec2 maxVec) {
		maxVec.x = (maxVec.x < testVec.x) ? testVec.x : maxVec.x;
		maxVec.y = (maxVec.y < testVec.y) ? testVec.y : maxVec.y;
	}
	
	//No need to declare this in every single AABB object.
	//This method updates the bounds of an AABB in case a change in position or orientation occurred.
	static public void update(Vec2[] hullNew, Vec2[] hullOld, Vec2 min, Vec2 max) {
		min.x = hullNew[0].x;
		min.y = hullNew[0].y;
		max.x = hullNew[0].x;
		min.y = hullNew[0].y;
		for (int i = 0; i < hullNew.length; ++i) {
			minVec2(hullNew[i], min);
			minVec2(hullOld[i], min);
			maxVec2(hullNew[i], max);
			maxVec2(hullOld[i], max);
		}
	}
}
