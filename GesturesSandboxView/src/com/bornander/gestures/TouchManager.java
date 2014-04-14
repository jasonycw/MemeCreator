package com.bornander.gestures;

import android.view.MotionEvent;

import com.bornander.math.Vector2D;

public class TouchManager {

	private final int maxNumberOfTouchPoints;

	private final Vector2D[] points;
	private final Vector2D[] previousPoints;

	public TouchManager(final int maxNumberOfTouchPoints) {
		this.maxNumberOfTouchPoints = maxNumberOfTouchPoints;

		points = new Vector2D[maxNumberOfTouchPoints];
		previousPoints = new Vector2D[maxNumberOfTouchPoints];
	}

	public boolean isPressed(int index) {
		return points[index] != null;
	}

	public int getPressCount() {
		int count = 0;
		for(Vector2D point : points) {
			if (point != null)
				++count;
		}
		return count;
	}

	public Vector2D moveDelta(int index) {

		if (isPressed(index)) {
			Vector2D previous = previousPoints[index] != null ? previousPoints[index] : points[index];
			return Vector2D.subtract(points[index], previous);
		}
		else {
			return new Vector2D();
		}
	}

	private static Vector2D getVector(Vector2D a, Vector2D b) {
		if (a == null || b == null)
			throw new RuntimeException("can't do this on nulls");

		return Vector2D.subtract(b, a);
	}

	public Vector2D getPoint(int index) {
		return points[index] != null ? points[index] : new Vector2D();
	}

	public Vector2D getPreviousPoint(int index) {
		return previousPoints[index] != null ? previousPoints[index] : new Vector2D();
	}

	public Vector2D getVector(int indexA, int indexB) {
		return getVector(points[indexA], points[indexB]);
	}

	public Vector2D getPreviousVector(int indexA, int indexB) {
		if (previousPoints[indexA] == null || previousPoints[indexB] == null)
			return getVector(points[indexA], points[indexB]);
		else
			return getVector(previousPoints[indexA], previousPoints[indexB]);
	}

	public void update(MotionEvent event) {
	   int actionCode = event.getAction() & MotionEvent.ACTION_MASK;

	   if (actionCode == MotionEvent.ACTION_POINTER_UP || actionCode == MotionEvent.ACTION_UP) {
		   int index = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		   previousPoints[index] = points[index] = null;
	   }
	   else {
			for(int i = 0; i < maxNumberOfTouchPoints; ++i) {
				if (i < event.getPointerCount()) {
					int index = event.getPointerId(i);

					Vector2D newPoint = new Vector2D(event.getX(i), event.getY(i));

					if (points[index] == null)
						points[index] = newPoint;
					else {
						if (previousPoints[index] != null) {
							previousPoints[index].set(points[index]);
						}
						else {
							previousPoints[index] = new Vector2D(newPoint);

						}

						if (Vector2D.subtract(points[index], newPoint).getLength() < 64)
							points[index].set(newPoint);
					}
				}
				else {
				   previousPoints[i] = points[i] = null;
				}
			}
	   }
	}
}
