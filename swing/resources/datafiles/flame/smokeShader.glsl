uniform sampler2D uNormal;
uniform sampler2D uSmoke;

void main(void)
{
	vec3 normal = 2.0 * texture2D (uNormal, gl_TexCoord[0].st).rgb - 1.0;
	normal = normalize (normal);
	
	float lamberFactor = max (dot (gl_LightSource[1].position.xyz, normal), 0.0) ;
	
	lamberFactor = lamberFactor * .6 + .4;
	
	vec4 diffuseMaterial = texture2D (uSmoke, gl_TexCoord[0].st);
	diffuseMaterial.rgb = vec3(1);
	
	vec4 diffuseLight = gl_LightSource[1].diffuse;
	vec4 ambientLight = gl_LightSource[1].ambient;
	
	vec4 bump = vec4(lamberFactor,lamberFactor,lamberFactor,1);
	
	vec4 light = diffuseLight * bump + ambientLight;
	
	gl_FragColor =	gl_Color * diffuseMaterial * light;
	
}