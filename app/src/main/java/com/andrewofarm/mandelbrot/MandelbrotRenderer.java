package com.andrewofarm.mandelbrot;

import android.content.Context;
import static android.opengl.GLES20.*;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Andrew on 12/31/16.
 */

public class MandelbrotRenderer implements Renderer {

    private static final boolean LOGGER_ON = false;

    private static final int BYTES_PER_FLOAT = 4;

    private Context context;
    private float screenSize;

    private float[] projectionMatrix = new float[16];

    private static final int POSITION_COMPONENT_COUNT = 2;
    private final float[] vertices = new float[12];
    private FloatBuffer vertexBuffer;

    private MandelbrotShaderProgram shaderProgram;


    private float centerX = 0.0f, centerY = 0.0f;
    private float scale = 2.0f;
    private static final float MAX_SCALE = 5f;
    private static final float MIN_SCALE = 5e-6f;
    private int iterations;
    private static final float MIN_ITERATIONS = 50;
    private float velocityX = 0.0f, velocityY = 0.0f;
    private static final float FRICTION = 0.08f;


    public MandelbrotRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
//        glClearColor(0, 56f/255, 101f/255, 0);

        shaderProgram = new MandelbrotShaderProgram(context);

        vertexBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        //adjust for aspect ratio
        float aspectRatio;
        if (width > height) {
            screenSize = height;
            aspectRatio = (float) width / height;
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
            adjustVertices(aspectRatio, 1);
        } else {
            aspectRatio = (float) height / width;
            screenSize = width;
            Matrix.orthoM(projectionMatrix, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);
            adjustVertices(1, aspectRatio);
        }

        vertexBuffer.position(0);
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT);

        //fling
        centerX -= velocityX / 50;
        centerY += velocityY / 50;

        //slow down fling movement
        float speed = (float) Math.sqrt((velocityX * velocityX) + (velocityY * velocityY));
        if (speed > 0) {
            float speedScale = Math.max(1 - (FRICTION * scale / speed), 0);
            velocityX *= speedScale;
            velocityY *= speedScale;
        }

        //bind data
        glVertexAttribPointer(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aPositionLocation);

        iterations = (int) Math.max(100 - 50 * Math.log(scale), MIN_ITERATIONS);

        shaderProgram.useProgram();
        glUniformMatrix4fv(shaderProgram.uMatrixLocation, 1, false, projectionMatrix, 0);
        glUniform1f(shaderProgram.uScaleLocation, scale);
        glUniform2f(shaderProgram.uCenterLocation, centerX, centerY);
        glUniform1i(shaderProgram.uIterationsLocation, iterations);
        glUniform4f(shaderProgram.uSetColorLocation, 0, 0, 0, 1);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    private void adjustVertices(float x, float y) {
        //Triangle 1
        vertices[0] = -x;
        vertices[1] = -y;

        vertices[2] = x;
        vertices[3] = y;

        vertices[4] = -x;
        vertices[5] = y;

        //Triangle 2
        vertices[6] = -x;
        vertices[7] = -y;

        vertices[8] = x;
        vertices[9] = -y;

        vertices[10] = x;
        vertices[11] = y;
    }

//    public void handleTouchPress(float normalizedX, float normalizedY) {
//        dragFromX = normalizedX;
//        dragFromY = normalizedY;
//    }
//
//    public void handleTouchDrag(float normalizedX, float normalizedY) {
//        float dx = -(normalizedX - dragFromX) * scale * 2;
//        float dy = (normalizedY - dragFromY) * scale * 2;
//        centerX += dx;
//        centerY += dy;
////        iterations += (int) ((dragFromY - normalizedY) * 100);
////        Log.v("handleTouchDrag", "Iterations: " + iterations);
//        dragFromX = normalizedX;
//        dragFromY = normalizedY;
//    }

    public void handleOnScroll(float distanceX, float distanceY) {
        if (LOGGER_ON) {
            Log.d("handleOnScroll", "scroll gesture detected:" +
                    "\ndistanceX: " + distanceX +
                    "\ndistanceY: " + distanceY);
        }

        float dx = distanceX * scale * 2 / screenSize;
        float dy = -distanceY * scale * 2 / screenSize;
        centerX += dx;
        centerY += dy;
        velocityX = 0;
        velocityY = 0;
    }

    public void handleOnFling(float velocityX, float velocityY) {
        if (LOGGER_ON) {
            Log.d("handleOnFling", "fling gesture detected:" +
                    "\nvelocityX: " + velocityX +
                    "\nvelocityY: " + velocityY);
        }

        this.velocityX = velocityX / screenSize * scale;
        this.velocityY = velocityY / screenSize * scale;
    }

    public void handleOnScale(float scaleFactor) {
        if (LOGGER_ON) {
            Log.d("handleOnScale", "scale gesture detected:" +
                    "\nscaleFactor: " + scaleFactor);
        }

        scale /= (scaleFactor - 1) * 2 + 1;
        scale = Math.min(Math.max(scale, MIN_SCALE), MAX_SCALE);
    }
}
