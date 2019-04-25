precision highp float;

uniform mat4 u_Matrix;

attribute vec4 a_Position;

varying vec4 v_Position;

void main() {
    v_Position = a_Position;

    gl_Position = u_Matrix * a_Position;
}
