#version 140
out vec4 FragColor;

in vec2 v_pos;

uniform vec3 topColor;
uniform vec3 bottomColor;
uniform int worldAligned;
uniform mat3 viewToWorld;
uniform vec2 inverseProjectionScale;

void main()
{
	float t;
	if (worldAligned != 0) {
		// Reconstruct this pixel's view ray and express its elevation relative to world up.
		// The transition follows the horizon when the camera pitches instead of remaining
		// glued to the viewport.
		vec3 viewRay = normalize(vec3(v_pos * inverseProjectionScale, -1.0));
		float elevation = normalize(viewToWorld * viewRay).y;
		t = smoothstep(-0.25, 0.35, elevation);
	} else {
		// Ordinary design-view gradients remain screen aligned.
		t = (v_pos.y + 1.0) / 2.0;
	}
	vec3 finalColor = mix(bottomColor, topColor, t);
    FragColor = vec4(finalColor, 1.0);
}
