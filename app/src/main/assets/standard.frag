precision lowp float;
uniform lowp vec4 materialColor;
varying lowp vec4 position;
varying float diffuse;
varying vec3 normalVector;
void main()
{
    gl_FragColor = vec4(normalVector,1.0);
}
