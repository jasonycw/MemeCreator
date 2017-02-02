package cs4295.gesture;

import android.view.MotionEvent;

import cs4295.math.Vector2D;

public class TouchManager {

	private final int maxNumberOfTouchPoints;
	private final Vector2D[] points;
	private final Vector2D[] previousPoints;

	public TouchManager(final int maxNumberOfTouchPoints) {
		this.maxNumberOfTouchPoints = maxNumberOfTouchPoints;

		points = new Vector2D[maxNumberOfTouchPoints];
		previousPoints = new Vector2D[maxNumberOfTouchPoints];
	}

	// Returns true if touch index is pressed
	public boolean isPressed(int index) {
		return points[index] != null;
	}

	// Returns the number of current touch points
	public int getPressCount() {
		int count = 0;
		for (Vector2D point : points) {
			if (point != null)
				++count;
		}
		return count;
	}

	// Returns the delta between current and previous touch with index 'index'
	public Vector2D moveDelta(int index) {
		if (isPressed(index)) {
			Vector2D previous = previousPoints[index] != null ? previousPoints[index]
					: points[index];
			return Vector2D.subtract(points[index], previous);
		} else {
			return new Vector2D();
		}
	}

	// Returns the delta between all current and previous touches
	public Vector2D moveDelta() {
		Vector2D[] allPreviousPoints = new Vector2D[maxNumberOfTouchPoints];
		Vector2D result = new Vector2D();

		// Get the collection of all the previousPoints
		for (int i = 0; i < maxNumberOfTouchPoints; ++i)
			if (isPressed(i))
				allPreviousPoints[i] = previousPoints[i] != null ? previousPoints[i]
						: points[i];

		float totalX, totalY, n;
		Vector2D currentPoint, previousPoint;

		// Get the averaged previousPoint
		totalX = 0;
		totalY = 0;
		n = 0;
		for (int i = 0; i < maxNumberOfTouchPoints; ++i)
			if (isPressed(i)) {
				totalX += allPreviousPoints[i].getX();
				totalY += allPreviousPoints[i].getY();
				n++;
			}
		previousPoint = new Vector2D(totalX / n, totalY / n);

		// Get the averaged currentPoint
		totalX = 0;
		totalY = 0;
		n = 0;
		for (int i = 0; i < maxNumberOfTouchPoints; ++i)
			if (isPressed(i)) {
				totalX += points[i].getX();
				totalY += points[i].getY();
				n++;
			}
		currentPoint = new Vector2D(totalX / n, totalY / n);

		result = Vector2D.subtract(currentPoint, previousPoint);
		return result;
	}

	// Return the middle point between all points
	public Vector2D getMiddlePoint() {
		Vector2D middle;

		float totalX, totalY, n;
		totalX = 0;
		totalY = 0;
		n = 0;
		for (int i = 0; i < maxNumberOfTouchPoints; ++i)
			if (isPressed(i)) {
				totalX += points[i].getX();
				totalY += points[i].getY();
				n++;
			}
		middle = new Vector2D(totalX / n, totalY / n);
		return middle;
	}

	// Get the vector that is the difference with vector a and b
	private static Vector2D getVector(Vector2D a, Vector2D b) {
		if (a == null || b == null)
			throw new RuntimeException("can't do this on nulls");

		return Vector2D.subtract(b, a);
	}

	// The the (x, y) point for touch index
	public Vector2D getPoint(int index) {
		return points[index] != null ? points[index] : new Vector2D();
	}

	// The the (x, y) point for previous touch index
	public Vector2D getPreviousPoint(int index) {
		return previousPoints[index] != null ? previousPoints[index]
				: new Vector2D();
	}

	// The the vector that is the difference between two simultenous touches
	public Vector2D getVector(int indexA, int indexB) {
		return getVector(points[indexA], points[indexB]);
	}

	// The the vector that is the difference between two previous simultenous
	// touches
	public Vector2D getPreviousVector(int indexA, int indexB) {
		if (previousPoints[indexA] == null || previousPoints[indexB] == null)
			return getVector(points[indexA], points[indexB]);
		else
			return getVector(previousPoints[indexA], previousPoints[indexB]);
	}

	// This method is responsible for inspecting the event and populating the
	// backing arrays
	//
	// The first thing to do is to figure out what kind of event this was, was
	// caused by the
	// user pressing the screen, dragging his/hers finger across the screen of
	// was the finger
	// lifted off the screen.
	//
	// This information is contained in the action, but a bit of bit masking is
	// required in
	// order to make sense of it.
	public void update(MotionEvent event) {
		int actionCode = event.getAction() & MotionEvent.ACTION_MASK;

		if (actionCode == MotionEvent.ACTION_POINTER_UP
				|| actionCode == MotionEvent.ACTION_UP) {
			int index = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			previousPoints[index] = points[index] = null;
		} else {
			for (int i = 0; i < maxNumberOfTouchPoints; ++i) {
				if (i < event.getPointerCount()) {
					int index = event.getPointerId(i);

					Vector2D newPoint = new Vector2D(event.getX(i),
							event.getY(i));

					if (points[index] == null)
						points[index] = newPoint;
					else {
						if (previousPoints[index] != null) {
							previousPoints[index].set(points[index]);
						} else {
							previousPoints[index] = new Vector2D(newPoint);

						}

						// Sanity check, if it moves by too much then ignore it
						if (Vector2D.subtract(points[index], newPoint)
								.getLength() > 0)
							points[index].set(newPoint);
					}
				} else {
					previousPoints[i] = points[i] = null;
				}
			}
		}
	}
}
