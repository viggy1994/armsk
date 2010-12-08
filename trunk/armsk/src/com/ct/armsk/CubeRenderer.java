/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ct.armsk;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;


/**
 * Render a pair of tumbling cubes.
 */

class CubeRenderer implements GLSurfaceView.Renderer {
	
    float[] homography = new float[]{1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1};
    public CubeRenderer(boolean useTranslucentBackground) {
        mTranslucentBackground = useTranslucentBackground;
        mCube = new Cube();
    }

    public void onDrawFrame(GL10 gl) {
        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D objects
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
		//GLU.gluLookAt(gl, 0, 0, 4.2f, 0, 0, 0, 0, 1, 0);

        gl.glLoadMatrixf(homography, 0);


        gl.glTranslatef(0, 0, -6.0f);
        gl.glRotatef(50, 1, 0, 0);

        gl.glRotatef(20, 0, 1, 1);
        //gl.glTranslatef(rotation[0], rotation[1], rotation[2]);
        //gl.glRotatef(mAngle,        0, 1, 0);
        //gl.glRotatef(mAngle*0.25f,  0, 0, 1);

        
        //gl.glRotatef(mAngle*2.0f, 0, 1, 1);
       

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);


        mCube.draw(gl);

        mAngle += 1.2f;  
        
        
        
        //gl.glRotatef(mAngle*2.0f, 0, 0, 1);
        //gl.glTranslatef(0.5f, 0.5f, 0.5f);

        //mCube.draw(gl);

        //mAngle += 1.2f;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
         gl.glViewport(0, 0, width, height);

         /*
          * Set our projection matrix. This doesn't have to be done
          * each time we draw, but usually a new projection needs to
          * be set when the viewport is resized.
          */

         float ratio = (float) width / height;
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                 GL10.GL_FASTEST);

         if (mTranslucentBackground) {
             gl.glClearColor(0,0,0,0);
         } else {
             gl.glClearColor(1,1,1,1);
         }
         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glShadeModel(GL10.GL_SMOOTH);
         gl.glEnable(GL10.GL_DEPTH_TEST);
    }
    private boolean mTranslucentBackground;
    private Cube mCube;
    private float mAngle;

    float[] rotation = new float[3];
    
    //boolean newMatrix = false;
    FloatBuffer fbuffer = FloatBuffer.allocate(1024);

	public void updateHomography(float[] transform) {
		/*newMatrix = true;
		int k = 0;
		for(int i = 0; i < 4; i=i+1){
			for(int j = 0; j < 4; j=j+1){
				if(k == 0 || k == 5 || k == 10 || k == 15){
					homography[k] = 1.0f;
					k++;
				}
				else{
					homography[k] = transform[i][j];
					k++;
				}
			}
		}*/
		homography = transform;
		//newMatrix = true;
	}
	
	public void updateRotation(float[] rotv){
		for (int j = 0; j < 3; j=j+1){
			rotation[j] = rotv[j];
		}
	}
}
