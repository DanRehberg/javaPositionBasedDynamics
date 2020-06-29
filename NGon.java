package pbd;

import javafx.geometry.*;
import javafx.scene.shape.Line;

/*
 * Author: Daniel Rehberg
 * 
 * This class builds an N-Gon, this merely meaning the object has to be a polygon requiring a closed loop.
 * This class will throw exceptions if too few vertices are defined.
 * This class is expected to behave in simulations of Dynamics and so extends Mechanics, offering its own
 * 	implementation of the Integration interface's methods and an override of Mechanics update method.
 * This class is primarily used for Position Based Dynamics (PBD) rather than as a rigid body.
 */

public class NGon extends Mechanics{
	
	//Fields
	private Vec2 origin;//The center of the NGon.
	private Vec2[] vertices;//The coordinate data for each vertex.
	private Line[] lines;//The lines needed to show the polygon.
	private double radius;//This is the radius from the center (origin) to a vertex -- uniformly spaced vertices from the origin.
	private double vertexDistance;//This is the distance between any of the uniformly spaced vertices on th hull.
	
	//This constructor builds an NGon, if valid, and uses a constant radius to position the vertices around the origin.
	public NGon( int vertexCount) throws IllegalArgumentException {
		 super(5);
		//magnitude of 5.0 for now
		if (vertexCount < 3) throw new IllegalArgumentException("Need three or more vertices for a closed loop.");
		if (vertexCount > 60) throw new IllegalArgumentException("This N-Gon looks suspiciously like a circle with " + vertexCount + " vertices.");
		
		//Triangle{0,1,2} example; This continues for any N-Gon though.
		//Vert[0] -> end of line[2] and start of line[0]
		//Vert[1] -> end of line[0] and start of line[1]
		//Vert[2] -> end of line[1] and start of line[2]
		
		//Very straightforward indexing pattern for the lines with the vertices:
		//	~Each vertex is associated with two lines.
		//		-This corresponds to one start and one end for a line.
		//	~For the vertex indices (0, N-1] the pattern is simple.
		//		-[i] is the end pos for line[i-1] and the start pos for line[i]
		//	~The Exception case is for the zeroth vertex.
		//		-The start pos is still line[0] but the end pos is at line[n-1]
		
		vertices = new Vec2[vertexCount];
		origin = new Vec2(480.0, 270.0);
		lines = new Line[vertexCount];
		//initialize the old vertices and velocities needed for mechanical simulation
		oldCenter = new Vec2(origin.x, origin.y);
		oldVertices = new Vec2[vertexCount];
		velocity = new Vec2[vertexCount];

		//The NGon is simply vertices spaced equally apart around a circle.
		double rads = 0;
		double radIncrement = (2.0 * Math.PI) / ((double)vertexCount);
		
		for (int i = 0; i < vertexCount; ++i) {
			//Use an initial position right above the origin.
			double posX = -50.0 + origin.getX(), posY = -50.0 + origin.getY();
			if (i == 0)this.radius = Math.sqrt(50 * 50 * 2);
			if (i == 2) {
				//Build the distance between vertices
				Vec2 ab = new Vec2(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y);
				vertexDistance = Math.sqrt(ab.x * ab.x + ab.y * ab.y);
			}
			double xPrime = origin.getX() + (Math.cos(rads) * (posX - origin.getX()) - Math.sin(rads) * (posY - origin.getY()));
			double yPrime = origin.getY() + (Math.sin(rads) * (posX - origin.getX()) + Math.cos(rads) * (posY - origin.getY()));
			vertices[i] = new Vec2(xPrime, yPrime);
			oldVertices[i] = new Vec2(xPrime, yPrime);
			velocity[i] = new Vec2(0.0, 0.0);
			lines[i] = new Line();
			rads += radIncrement;
		}
		
		//build the lines
		for (int i = 0; i < vertexCount; ++i) {
			lines[i].setStartX(vertices[i].x);
			lines[i].setStartY(vertices[i].y);
			if (i == (vertexCount - 1)) {
				//exception case where the zeroth index is needed
				lines[i].setEndX(vertices[0].x);
				lines[i].setEndY(vertices[0].y);
			} else {
				lines[i].setEndX(vertices[i + 1].x);
				lines[i].setEndY(vertices[i + 1].y);
			}
			lines[i].setStyle("-fx-stroke: #880000;");
			lines[i].setSmooth(false);
		}
		
		//Generate the hitbox so it is ready to be used
		col.update(oldVertices, vertices, col.min, col.max);
	}
	
	//This constructor allows for a specific origin and dimension for the object to be initialized at.
	public NGon(int vertexCount, double mass, Vec2 center, double radius) throws IllegalArgumentException {
		super(mass);
		if (vertexCount < 3) throw new IllegalArgumentException("Need three or more vertices for a closed loop.");
		if (vertexCount > 60) throw new IllegalArgumentException("This N-Gon looks suspiciously like a circle with " + vertexCount + " vertices.");
		if (radius == 0.0) throw new IllegalArgumentException("The radius for vertices around their origin needs to be a real number.");
		
		vertices = new Vec2[vertexCount];
		origin = new Vec2(center.x, center.y);
		lines = new Line[vertexCount];
		//initialize the old vertices and velocities needed for mechanical simulation
		oldCenter = new Vec2(origin.x, origin.y);
		oldVertices = new Vec2[vertexCount];
		velocity = new Vec2[vertexCount];

		double rads = 0;
		double radIncrement = (2.0 * Math.PI) / ((double)vertexCount);
		
		for (int i = 0; i < vertexCount; ++i) {
			//Use an initial position right above the origin.
			double posX = -radius + origin.getX(), posY = -radius + origin.getY();
			if (i == 0)this.radius = Math.sqrt(radius * radius * 2);
			double xPrime = origin.getX() + (Math.cos(rads) * (posX - origin.getX()) - Math.sin(rads) * (posY - origin.getY()));
			double yPrime = origin.getY() + (Math.sin(rads) * (posX - origin.getX()) + Math.cos(rads) * (posY - origin.getY()));
			vertices[i] = new Vec2(xPrime, yPrime);
			oldVertices[i] = new Vec2(xPrime, yPrime);
			velocity[i] = new Vec2(0.0, 0.0);
			lines[i] = new Line();
			rads += radIncrement;
		}
		
		//build the lines
		for (int i = 0; i < vertexCount; ++i) {
			lines[i].setStartX(vertices[i].x);
			lines[i].setStartY(vertices[i].y);
			if (i == (vertexCount - 1)) {
				//exception case where the zeroth index is needed
				lines[i].setEndX(vertices[0].x);
				lines[i].setEndY(vertices[0].y);
			} else {
				lines[i].setEndX(vertices[i + 1].x);
				lines[i].setEndY(vertices[i + 1].y);
			}
			lines[i].setStyle("-fx-stroke: #880000;");//Antialiasing if possible
			lines[i].setSmooth(false);
		}
		
		//Generate the hitbox so it is ready to be used
		col.update(oldVertices, vertices, col.min, col.max);
	}
	
	//This method returns the lines rendered by the application.
	public Line[] getLines() {
		return this.lines;
	}
	
	//This method returns the origin coordinate.
	public Vec2 getOrigin() {
		return this.origin;
	}
	
	//This method returns the distance from the origin to a vertex.
	public double getRadius() {
		return this.radius;
	}
	
	//This method returns the distance between vertices on a shared line.
	public double getVertexDistance() {
		return this.vertexDistance;
	}
	
	//This returns all of the coordinate data.
	public Vec2[] getVertices() {
		return this.vertices;
	}
	
	//Build the specialized integration method for the body type.
	//	N-Gon has soft body potential and needs to update each vertex independently
	public void integrateAcceleration(double dT, double aX, double aY) {
		for (Vec2 vel : this.velocity) {
			vel.x += dT * dT * aX;
			vel.y += dT * dT * aY;
		}
	}
	
	//This sets adds the change in position to the vertices.
	public void integrateVelocity(double dT) {
		for (int i = 0; i < vertices.length; ++i) {
			oldVertices[i].x = vertices[i].x;
			oldVertices[i].y = vertices[i].y;
			vertices[i].x += dT * this.velocity[i].x;
			vertices[i].y += dT * this.velocity[i].y;
			//lines[i].setStartX(vertices[i].x += dT * this.velocity[i].x);
			//lines[i].setStartY(vertices[i].y += dT * this.velocity[i].y);
			//The position is already integrated with the new velocity above, so the vertex is
			//	ready to be used to set the second position it affects on a line segment.
			/*if (i == 0) {
				lines[vertices.length - 1].setEndX(vertices[i].x);
				lines[vertices.length - 1].setEndY(vertices[i].y);
			} else {
				lines[i - 1].setEndY(vertices[i].x);
				lines[i - 1].setEndY(vertices[i].y);
			}*/
		}
		setLinePositions();
	}
	
	//This updates the lines rendered with their new positions.
	public void setLinePositions() {
		for (int i = 0; i < lines.length; ++i) {
			lines[i].setStartX(vertices[i].x);
			lines[i].setStartY(vertices[i].y);
			if (i == lines.length - 1) {
				lines[i].setEndX(vertices[0].x);
				lines[i].setEndY(vertices[0].y);
			} else {
				lines[i].setEndX(vertices[i + 1].x);
				lines[i].setEndY(vertices[i + 1].y);
			}
		}
	}
	
	//This method modifies the velocity of each vertex by examining the change in position for a frame divided
	//	by a delta described for that frame.
	public void verlet(double dT) {
		for (int i = 0; i < vertices.length; ++i) {
			velocity[i].x = (oldVertices[i].x - vertices[i].x) / dT;
			velocity[i].y = (oldVertices[i].y - vertices[i].y) / dT;
		}
	}
	
	//Ensure the appropriate update method is invoked, rather than the default Mechanics method.
	@Override
	final public boolean update(double dT, Collision viewBounds) {
		
		if (mass > 0) {
			integrateAcceleration(dT, 0.0, 0.98);//Gravity constant included in here
			integrateVelocity(dT);
			
			col.update(oldVertices, vertices, col.min, col.max);
			origin.x = (col.min.x + col.max.x) * 0.5;
			origin.y = (col.min.y + col.max.y) * 0.5; 
			if (!Collision.intersection(col, viewBounds)) {
				return false;
			}
		}
		
		return true;
	}
}
