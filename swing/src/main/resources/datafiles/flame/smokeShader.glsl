uniform sampler2D uNormal;
uniform sampler2D uSmoke;
uniform float z;

void main(void)
{
	vec3 normal = 2.0 * texture2D (uNormal, gl_TexCoord[0].st).rgb - 1.0;

	
	//Load only the Alpha from the smoke texture
	vec4 diffuseMaterial = texture2D (uSmoke, gl_TexCoord[0].st);
	diffuseMaterial.rgb = vec3(1);
	
	//Get Ambient light
	vec4 ambientLight = gl_LightSource[1].ambient;
	
	//Calculate diffuse light from "sun"
	vec3 diffuseNormal = normal;
	diffuseNormal.b = diffuseNormal.b * 0.5;
	diffuseNormal = normalize (gl_NormalMatrix * diffuseNormal);
	vec4 diffuseLight = gl_LightSource[1].diffuse;
	float diffuseFactor = max (dot (gl_LightSource[1].position.xyz, diffuseNormal), 0.0) ;
	diffuseFactor = diffuseFactor * .6 + .4;
	vec4 diffuseBump = vec4(diffuseFactor,diffuseFactor,diffuseFactor,1);
	
	
	vec4 flameColor = gl_LightSource[2].diffuse;
	float flameFactor =  max (dot (vec3(0,0,1), diffuseNormal), 0.0);
	flameFactor = flameFactor * (1.0 - z / 1.0);
	flameFactor = flameFactor * diffuseMaterial.a * gl_Color.a * 10.0;
	flameFactor = clamp(flameFactor,0.0,1.0);
	
	vec4 light = diffuseLight * diffuseBump + ambientLight;
	
	gl_FragColor =	gl_Color * diffuseMaterial * light  + flameColor * flameFactor;
}