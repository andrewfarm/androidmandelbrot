package com.andrewofarm.mandelbrot;

import android.content.Context;
import static android.opengl.GLES20.*;

/**
 * Created by Andrew on 12/31/16.
 */

public class MandelbrotShaderProgram {

    //vertex shader variables
    private static final String U_MATRIX = "u_Matrix";
    public final int uMatrixLocation;
    private static final String A_POSITION = "a_Position";
    public final int aPositionLocation;

    //fragment shader variables
    private static final String U_SCALE = "u_Scale";
    public final int uScaleLocation;
    private static final String U_CENTER = "u_Center";
    public final int uCenterLocation;
    private static final String U_ITERATIONS = "u_Iterations";
    public final int uIterationsLocation;
    private static final String U_SET_COLOR = "u_SetColor";
    public final int uSetColorLocation;

    protected final int programID;

    MandelbrotShaderProgram(Context context) {
        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.mandelbrot_fragment_shader);
        programID = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);

        //retrieve variable locations
        uMatrixLocation = glGetUniformLocation(programID, U_MATRIX);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        uScaleLocation = glGetUniformLocation(programID, U_SCALE);
        uCenterLocation = glGetUniformLocation(programID, U_CENTER);
        uIterationsLocation = glGetUniformLocation(programID, U_ITERATIONS);
        uSetColorLocation = glGetUniformLocation(programID, U_SET_COLOR);
    }

    public void useProgram() {
        glUseProgram(programID);
    }
}
