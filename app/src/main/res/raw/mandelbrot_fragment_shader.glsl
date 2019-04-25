precision highp float;

const float THRESHOLD = 2.0;
const float THRESHOLD_SQ = 4.0;

const int EXTRA_ITERATIONS = 2;

const float COLOR_SCALE = 1.0 / 32.0;

uniform float u_Scale;
uniform vec2 u_Center;
uniform int u_Iterations;
uniform vec4 u_SetColor;

varying vec4 v_Position;

struct Complex {
    float re, im;
};

float mset(Complex);

void main() {
    Complex c = Complex(
        v_Position.x * u_Scale + u_Center.x,
        v_Position.y * u_Scale + u_Center.y);

    float m = mset(c);
    if (m < 0.0) {
        gl_FragColor = u_SetColor;
    } else {
        float f = m * COLOR_SCALE;
        f = f - float (int (f));
        float r, g, b;

        //piecewise function
        if (f < 0.092) {
        	r = 0.0;
        	g = 3.118 * f;
       		b = 3.246 * f + 0.255;
       	} else if (f < 0.179) {
       		r = 4.423 * f - 0.406;
       		g = 3.159 * f + 0.003;
       		b = 3.205 * f + 0.259;
       	} else if (f < 0.318) {
       		r = 4.418 * f - 0.405;
       		g = 3.124 * f + 0.003;
       		b = 1.210 * f + 0.615;
       	} else if (f < 0.674) {
   			r = -0.220 * f + 1.070;
       		g = -0.948 * f + 1.298;
      		b = -2.811 * f + 1.894;
      	} else if (f < 0.885) {
      		r = -3.579 * f + 3.333;
      		g = -3.115 * f + 2.758;
       		b = 0.834 * f - 0.562;
       	} else {
      		r = -1.435 * f + 1.435;
        	g = 0.0;
        	b = 0.854 * f - 0.580;
        }

        gl_FragColor = vec4(r, g, b, 1);
    }
}

Complex iterate(Complex z, Complex c) {
    // z = z^2 + c
    return Complex(
        (z.re * z.re) - (z.im * z.im) + c.re,
        2.0 * z.re * z.im + c.im);
}

/*
Returns the number of iterations of z = z^2 + c before the magnitude of c passes THRESHOLD,
or -1 if it does not pass THRESHOLD (i.e., c could be in the Mandelbrot Set)
*/
float mset(Complex c) {
    Complex z = c;
    for (int i = 0; i < u_Iterations; i++) {
        if (z.re * z.re + z.im * z.im > THRESHOLD_SQ) {
            for (int j = 0; j < EXTRA_ITERATIONS; j++) {
                z = iterate(z, c);
            }
            float returnValue = float (i) + 1.0
                - log(log(sqrt(z.re * z.re + z.im * z.im)) * 1.442695) * 1.442695;
            if (returnValue < 0.0) {
                returnValue = 0.0;
            }
            return returnValue;
//            return float (i);
        }

        z = iterate(z, c);
    }
    return -1.0;
}
