package com.bornander.math;

public class Vector2D {

	private float x;
	private float y;

	public Vector2D() {
	}

	public Vector2D(Vector2D v) {
		this.x = v.x;
		this.y = v.y;
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getLength() {
		return (float)Math.sqrt(x * x + y * y);
	}

	public Vector2D set(Vector2D other) {
		x = other.getX();
		y = other.getY();
		return this;
	}

	public Vector2D set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2D add(Vector2D value) {
		this.x += value.getX();
		this.y += value.getY();
		return this;
	}

	public static Vector2D subtract(Vector2D lhs, Vector2D rhs) {
		return new Vector2D(lhs.x - rhs.x, lhs.y - rhs.y);
	}

	public static float getDistance(Vector2D lhs, Vector2D rhs) {
		Vector2D delta = Vector2D.subtract(lhs, rhs);
		return delta.getLength();
	}

	public static float getSignedAngleBetween(Vector2D a, Vector2D b) {
		Vector2D na = getNormalized(a);
		Vector2D nb = getNormalized(b);

		return (float)(Math.atan2(nb.y, nb.x) - Math.atan2(na.y, na.x));
	}

	public static Vector2D getNormalized(Vector2D v) {
		float l = v.getLength();
		if (l == 0)
			return new Vector2D();
		else
			return new Vector2D(v.x / l, v.y / l);

	}

	@Override
	public String toString() {
		return String.format("(%.4f, %.4f)", x, y);
	}
}
