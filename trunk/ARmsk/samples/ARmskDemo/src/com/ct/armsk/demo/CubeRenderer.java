
/*
 * Copyright 2010, 2011 Project ARmsk
 * 
 * This file is part of ARmsk.
 * ARmsk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * ARmsk is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ARmsk.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.ct.armsk.demo;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/**
 * Render a pair of tumbling cubes.
 */

class CubeRenderer implements GLSurfaceView.Renderer {

	boolean drawCube = false;
	
	float scale = 1.0f;
	
	float[] homography = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0,  0.0f,
			0.0f, 0.0f, 1 };
	
	float[] euler = new float[3];

	public CubeRenderer(boolean useTranslucentBackground) {
		mTranslucentBackground = useTranslucentBackground;
		mCube = new Cube();
	}
	float angle = 0.5f;
	public void onDrawFrame(GL10 gl) {
		/*
		 * Usually, the first thing one might want to do is to clear the screen.
		 * The most efficient way of doing this is to use glClear().
		 */

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		
	
		/*float[] rotate = new float[] { 1, 0, 0, 0, 0, (float) Math.cos(angle), - (float) Math.sin(angle), 0, 0, (float) Math.sin(angle), (float) Math.cos(angle), 0, 0,
				0, -6, 1 };*/
		
		angle = angle + 0.02f;
		
		
		/*
		 * Now we're ready to draw some 3D objects
		 */
		//gl.glScalef(1.0f/480f, 1.0f/800f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		//gl.glScalef(1.0f/30.f, 1.0f/30.f, 1.0f/30.f);
		//gl.glTranslatef(0.0f, 0.0f, -600.0f);
		gl.glLoadMatrixf(homography, 0);
		gl.glScalef(scale, scale, scale);
		//gl.glScalef(1.0f, 1.0f, 1.0f);
		
		
		//gl.glTranslatef(0.0f, 0.0f, -3.0f);

		//gl.glScalef(30.0f , 30.0f, 1.0f);
		//gl.glTranslatef(-400.0f, -240.0f, 0.0f);
		
		//GLU.gluLookAt(gl, 0, 0, 1.8f, 0, 0, 0, 0, 1, 0);
		
		//gl.glTranslatef(-300.0f/480f,50.0f/800f,-9.0f);
		//gl.glTranslatef(5.5f,2.0f,-12.0f);
		
		//mCube.draw(gl);
		//gl.glTranslatef(-20.0f,-20.0f,0.0f);
		//gl.glScalef(1.0f/480f, 1.0f/800f, 1.0f);
		// mCube.draw(gl);
		// GLU.gluLookAt(gl, 0, 0, 4.2f, 0, 0, 0, 0, 1, 0);
		
		/*
		 * gl.glRotatef(euler[0], 1.0f, 0, 0); gl.glRotatef(euler[1], 0, 1.0f,
		 * 0); gl.glRotatef(euler[2], 0, 0, 1.0f);
		 */

		// gl.glRotatef(50, 1, 0, 0);

		// gl.glRotatef(20, 0, 1, 1);
		// gl.glTranslatef(rotation[0], rotation[1], rotation[2]);
		// gl.glRotatef(mAngle, 0, 1, 0);
		// gl.glRotatef(mAngle*0.25f, 0, 0, 1);

		// gl.glRotatef(mAngle*2.0f, 0, 1, 1);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		if (drawCube)
			mCube.draw(gl);

		// gl.glRotatef(mAngle*2.0f, 0, 0, 1);
		// gl.glTranslatef(0.5f, 0.5f, 0.5f);

		 //mCube.draw(gl);

		// mAngle += 1.2f;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */

		
		//float ratio = (float) width / height;
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		//GLU.gluOrtho2D(gl, -0.2f, 0.2f, 800.0f, 480.0f);
		
		//gl.glOrthof(-ratio, ratio, -1, 1, 1, 10);
		//gl.glFrustumf(-ratio, ratio, -1, 1, 1, 15);
		//GLU.gluPerspective(gl, 102.89f, ratio, 1, 1000);
		GLU.gluPerspective(gl, 74.0678f, ratio, 1, 1000);
		//gl.glLoadIdentity();
		//gl.glOrthof(-ratio, ratio, -1, 1, 1, 10);
		//gl.glFrustumf(left, right, bottom, top, zNear, zFar)
		//gl.glFrustumf(-2.5f, 2.5f, -1.5f, 1.5f, 1, 10);
		//
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		if (mTranslucentBackground) {
			gl.glClearColor(0, 0, 0, 0);
		} else {
			gl.glClearColor(1, 1, 1, 1);
		}
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

	private boolean mTranslucentBackground;
	private Cube mCube;
	//private float mAngle;

	float[] rotation = new float[3];

	// boolean newMatrix = false;
	FloatBuffer fbuffer = FloatBuffer.allocate(1024);

	public void updateHomography(float[] transform, float scalefactor, int draw) {

		 homography = transform;
		 scale = scalefactor;
		 if(draw == 1)
			 drawCube = true;
		 else
			 drawCube = false;
	}

	public void updateRotation(float[] rotv) {
		for (int j = 0; j < 3; j = j + 1) {
			rotation[j] = rotv[j];
		}
	}


}
