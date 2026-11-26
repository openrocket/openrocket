package info.openrocket.swing.gui.figure3d.photo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Computes render-only translations along the rocket's longitudinal axis. */
final class AxialExplosion {
	record Span(float min, float max) {
		float center() {
			return min + (max - min) / 2;
		}

		Span union(Span other) {
			return new Span(Math.min(min, other.min), Math.max(max, other.max));
		}
	}

	private AxialExplosion() {
	}

	/** Returns full-separation offsets in input order, preserving the overall axial center. */
	static float[] offsets(List<Span> spans) {
		float[] offsets = new float[spans.size()];
		if (spans.size() < 2) {
			return offsets;
		}
		List<Integer> order = new ArrayList<>();
		Span original = spans.get(0);
		for (int i = 0; i < spans.size(); i++) {
			order.add(i);
			original = original.union(spans.get(i));
		}
		// Stable sorting keeps coincident groups in scene order. Leave existing gaps intact.
		order.sort(Comparator.comparingDouble(i -> spans.get(i).center()));
		float gap = (original.max() - original.min()) * Math.min(0.04f, 0.25f / (spans.size() - 1));
		float left = spans.get(order.get(0)).min();
		float right = left;
		for (int index : order) {
			Span span = spans.get(index);
			float start = Math.max(span.min(), right);
			offsets[index] = start - span.min();
			right = start + (span.max() - span.min()) + gap;
		}
		float expandedCenter = left + (right - gap - left) / 2;
		float centerCorrection = expandedCenter - original.center();
		for (int i = 0; i < offsets.length; i++) {
			offsets[i] -= centerCorrection;
		}
		return offsets;
	}
}
