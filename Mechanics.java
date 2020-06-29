package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This class is for the mechanical features of a body, for whatever type.
 * This is a class to ensure the variety of child classes that could inherit this, eg:
 * 	soft body, rigid body, point, mesh, etc..., can upcast to a common abstract-ish parent
 * 	to offer open interactions of mixed object types.
 */

public class Mechanics implements Integration{
	
	//Fields for a mechanics object, though the AABB (col) and mass are the only definite concerns
	//	for this class as a parent (super) to manage.
	//The other fields are optional based on a derived class' needs.
	protected Collision col;//This holds an AABB and offers several methods for analyzing collision data.
	protected double mass;
	protected Vec2 oldCenter;
	protected Vec2[] oldVertices;
	protected Vec2[] velocity;//This could be one or many depending on the Class inheriting from Mechanics
	
	//The only constructor, determines if mass is invalid.
	//If mass is zero, this object is considered immoveable, i.e. static/stationary
	public Mechanics(double mass) throws IllegalArgumentException{
		if (mass < 0) throw new IllegalArgumentException("Mass needs to be ZERO or a POSITIVE value.");
		this.mass = mass;
		col = new Collision();
	}
	
	//This method is useful to test whether objects that are of the Mechanics class are broadly intersecting.
	public boolean intersectionBroad(Mechanics testBody) {
		return Collision.intersection(this.col, testBody.col);
	}
	
	//This method is useful to abstract, from the user, how the narrow phase collision test works.
	//This avoids complicating how objects are referenced by their hull edges (lines in 2D) with their
	//	center (origin) against the vertices of another object.
	static public boolean intersectionNarrow(Vec2 testPosition, Vec2[] referenceVertices, 
			Vec2 referenceCenter, int referenceTriangleIndex) throws IllegalArgumentException {
		if (referenceTriangleIndex < 0 || referenceTriangleIndex >= referenceVertices.length) {
			throw new IllegalArgumentException("No valid triangle at index " + referenceTriangleIndex);
		}
		if (referenceTriangleIndex == referenceVertices.length - 1) {
			return Collision.pointInTriangle(testPosition, referenceVertices[referenceTriangleIndex], 
					referenceVertices[0], referenceCenter);
		} 
		return Collision.pointInTriangle(testPosition, referenceVertices[referenceTriangleIndex], 
				referenceVertices[referenceTriangleIndex + 1], referenceCenter);
	}
	
	//This method is a default but may have its default implementation deprecated.
	public boolean update(double dT, Collision viewBounds) {
		//If the mass is equal to zero then the object is considered static.
		/*if (mass > 0) {
			integrateAcceleration(dT);
			integrateVelocity(dT);
			
		}*/
		return true;
	}
	
	
}
