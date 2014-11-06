attribute vec4 vertex;
attribute vec3 normal;


uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelViewMatrix;
uniform mat4 modelMatrix;
uniform mat4 normalMatrix;
uniform vec3 eye;
varying vec4 position;
varying vec3 normalVector;

varying float diffuse;
void main()
{
    position = vertex;
    normalVector = vec3(modelMatrix*vec4(normal,0.0));
    gl_Position = modelViewProjectionMatrix * vertex;
}
