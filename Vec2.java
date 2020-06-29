package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This class exists because the Point2D class does not offer a set method for its components,
 * 	only arithmetic; but it also does not offer the scalar variant of the 2D cross product -- 
 * 	I.E. the equivalent of solving for only one plane via cross product.
 */

public class Vec2 {
	//The fields, represent components of a 2-tuple
	public double x;
	public double y;
	
	//Initialize the fields to zero
	public Vec2() {
		x = 0.0;
		y = 0.0;
	}
	
	//Initializes the fields with custom values.
	public Vec2(double x_, double y_) {
		this.x = x_;
		this.y = y_;
	}
	
	//Returns the dot product of two vectors
	public static double dot(Vec2 a, Vec2 b) {
		return a.x * b.x + a.y * b.y;
	}
	
	//This cross product is not offered in the Point2D class
	//	instead Point2D assumes a 0 z component and returns a 
	//	perpendicular 3D vector.
	public static double cross(Vec2 a, Vec2 b) {
		return a.x * b.y - a.y * b.x;
	}
	
	//This method takes the perpendicular vector of AB and returns it normalized.
	public static Vec2 perpendicularNormal(Vec2 a, Vec2 b) {
		Vec2 ab = new Vec2(b.x - a.x, b.y - a.y);
		//Make ab perpendicular to itself.
		double x = ab.x;
		ab.x = -ab.y;
		ab.y = x;
		return normalize(ab);
	}
	
	//This method takes a vector and returns its normal.
	public static Vec2 normalize(Vec2 v) {
		//This could actually be the bit magic discovered by ID Software in the 90's rather than
		//	relying on Math's square-root implementation.
		double mag = v.x * v.x + v.y * v.y;
		mag = 1.0 / Math.sqrt(mag);
		return new Vec2(v.x * mag, v.y * mag);
	}
	
	//Returns the x, not necessary with public access of the field
	public double getX() {
		return x;
	}
	
	//Returns the y, not necessary with public access of the field
	public double getY() {
		return y;
	}
}
