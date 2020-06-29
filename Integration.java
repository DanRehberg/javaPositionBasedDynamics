package pbd;

/*
 * Author: Daniel Rehberg
 * 
 * This interface is used to define what methods/features need to be described for a mechanical object that 
 * 	will experience changes in velocity, and position.
 * Not using any impulses changes within the current implementation, only Verlet.
 */

public interface Integration {
	//public Vec2 velocity = null;
	
	//Implement the desired method for applying an impulse for a given body (rigid or soft)
	default void impulse(double translation, double rotation) {
		
	}
	
	//Implement the desired integration method for forces to change velocity.
	default void integrateAcceleration(double dt, double aX, double aY) {
		
	}
	
	//Implement the desired integration method for change in position.
	default void integrateVelocity(double dt) {
		
	}
	
	//Implement a Verlet based solution for a given body type (rigid or soft)
	default void verlet(double dT) {
		
	}
}
