package com.ct.armsk;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ct.armsk.jni.ARmsk;
import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;

public class armskdemo extends Activity {
	/** Called when the activity is first created. */
	private NativePreviewer mPreview;
	private GL2CameraViewer glview;
	final ARmsk armsk = new ARmsk();
	GLSurfaceView renderview;
	private SharedPreferences appPref;
	private int detection_method;
	boolean settingsChanged = false;
	boolean drawNumberOfKeypoints = false;
	boolean takingScreenShot = false;
	CubeRenderer arRenderer = new CubeRenderer(true);
	String path;

	/*float transform[][] = { { 1, 0, 0, -1 }, { 0, 1, 0, -1 }, { 0, 0, 1, 0 },
			{ 0, 0, 0, 1 } };*/
	
	float transform[] = new float[]{1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1};

	float rotv[] = { 0, 0, 0 };
	float transv[] = { 0, 0, 0 };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings");
		menu.add("Add template");
		menu.add("Start AR");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		defaultcallbackstack.addFirst(glview.getDrawCallback());
		if (item.getTitle().equals("Start AR")) {

			defaultcallbackstack.addFirst(new ARProcessor());
			Toast.makeText(this, "Initiate Augmented Reality",
					Toast.LENGTH_LONG).show();

			mPreview.addCallbackStack(defaultcallbackstack);

		}
		if (item.getTitle().equals("Settings")) {

			Intent intent = new Intent(this, EditPreferences.class);
			settingsChanged = true;

			startActivity(intent);
			return (true);

		}
		if (item.getTitle().equals("Add template")) {
			takingScreenShot = true;
			defaultcallbackstack.addFirst(new TemplateProcessor());

			Toast.makeText(this, "Template features added to the training set",
					Toast.LENGTH_LONG).show();
			mPreview.addCallbackStack(defaultcallbackstack);

		}
		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		appPref = PreferenceManager.getDefaultSharedPreferences(this);
		detection_method = 1; // Select FAST as default detection method

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		FrameLayout frame = new FrameLayout(this);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new NativePreviewer(getApplication(), 400, 400);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		params.height = getWindowManager().getDefaultDisplay().getHeight();
		params.width = (int) (params.height * 4.0 / 2.88);
		// params.width = getWindowManager().getDefaultDisplay().getWidth();

		LinearLayout vidlay = new LinearLayout(getApplication());

		vidlay.setGravity(Gravity.CENTER);
		vidlay.addView(mPreview, params);
		// frame.addView(vidlay);

		// make the glview overlay ontop of video preview
		mPreview.setZOrderMediaOverlay(false);

		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
		glview.setZOrderMediaOverlay(true);
		glview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		renderview = new GLSurfaceView(this);
		renderview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		renderview.setRenderer(arRenderer);

		renderview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		renderview.setZOrderMediaOverlay(true);
		frame.addView(renderview);
		frame.addView(vidlay);

		frame.addView(glview);

		setContentView(frame);

	}

	public void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// IMPORTANT
		// must tell the NativePreviewer of a pause
		// and the glview - so that they can release resources and start back up
		// properly
		// failing to do this will cause the application to crash with no
		// warning
		// on restart
		// clears the callback stack
		mPreview.onPause();

		glview.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (settingsChanged) {
			detection_method = Integer.parseInt(appPref.getString("detectPref",
					""));
			drawNumberOfKeypoints = appPref.getBoolean("displayKeypointsKey",
					false);
		}
		// resume the opengl viewer first
		glview.onResume();

		// add an initial callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();

		// SpamProcessor will be called first
		// cbstack.add(new SpamProcessor());

		// then the same idx and pool will be passed to
		// the glview callback -
		// so operate on the image at idx, and modify, and then
		// it will be drawn in the glview
		// or remove this, and call glview manually in SpamProcessor
		cbstack.add(glview.getDrawCallback());

		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();

	}

	public void buildMatrix() {
		/*
		 * for(int i = 0; i < 3; i=i+1){ for(int j = 0; j < 3; j=j+1){
		 * transform[i][j] = armsk.getElement(i,j,0); } }
		 */

		/*
		 * for(int i = 0; i < 3; i++){ rotv[i] =
		 * armsk.getElementRotationVector(i); } for(int i = 0; i < 3; i++){
		 * transv[i] = armsk.getElementTranslateVector(i); }
		 */
		transform = new float[]{armsk.get_d(0), armsk.get_d(3), armsk.get_d(6), 0, 
										armsk.get_d(1), armsk.get_d(4), armsk.get_d(7), 0,
										armsk.get_d(2), armsk.get_d(5), armsk.get_d(8), 0,
										0, 0, 0, 1};
		

		 
	
/*		transform[0] = armsk.get_d(0);
		transform[1] = armsk.get_d(3);
		 transform[2] = armsk.get_d(6);
		 transform[3] = 0;
		 transform[4] = armsk.get_d(1);
		 transform[5] = armsk.get_d(4);
		 transform[6] = armsk.get_d(7);
		 transform[7] = 0;
		 transform[8] = armsk.get_d(2);
		 transform[9] = armsk.get_d(5);
		 transform[10] = armsk.get_d(8);
		 transform[11] = 0;
		 transform[12] = armsk.get_d(12);
		

		transform[13] = 0;//armsk.getElementTranslateVector(0);
		transform[14] = 0;//armsk.getElementTranslateVector(1);
		transform[15] = 1;
		

		/*transform[0][0] = armsk.getElementRotationMatrix(0, 0);
		transform[0][1] = armsk.getElementRotationMatrix(0, 1);
		transform[0][2] = armsk.getElementRotationMatrix(0, 2);
		transform[1][0] = armsk.getElementRotationMatrix(1, 0);
		transform[1][1] = armsk.getElementRotationMatrix(1, 1);
		transform[1][2] = armsk.getElementRotationMatrix(1, 2);
		transform[2][0] = armsk.getElementRotationMatrix(2, 0);
		transform[2][1] = armsk.getElementRotationMatrix(2, 1);
		transform[2][2] = armsk.getElementRotationMatrix(2, 1);

		transform[3][0] = armsk.getElementTranslateVector(0);
		transform[3][1] = armsk.getElementTranslateVector(1);
		transform[3][2] = armsk.getElementTranslateVector(2);*/

	}

	// final processor so taht these processor callbacks can access it
	// final Processor processor = new Processor();

	class ARProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {

			armsk.processAR(idx, pool, detection_method, path);
			buildMatrix();
			arRenderer.updateHomography(transform);
			// arRenderer.updateRotation(rotv);
			/*
			 * armsk.drawText(idx, pool, " HMatrix:  " + armsk.getElement(0,0) +
			 * " " + armsk.getElement(0,1) + " " + armsk.getElement(0,2) + "\n"
			 * + armsk.getElement(1,0) + " " + armsk.getElement(1,1) + " " +
			 * armsk.getElement(1,2) + "\n" + armsk.getElement(2,0) + " " +
			 * armsk.getElement(2,1) + " " + armsk.getElement(2,2) + "\n");
			 */

			if (drawNumberOfKeypoints)
				armsk.drawText(idx, pool, "found "
						+ armsk.getNumberTemplateKeypoints() + " keypoints!");
		}

	}

	class TemplateProcessor implements NativeProcessor.PoolCallback {

		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			if (takingScreenShot) {
				takingScreenShot = false;

				/*
				 * File opencvdir = new
				 * File(Environment.getExternalStorageDirectory(),
				 * "-armskdemo"); if (!opencvdir.exists()) { opencvdir.mkdir();
				 * } File templateFile = new File(opencvdir, "template" + new
				 * Date().getTime() + ".jpg");
				 */
				armsk.setTemplate(idx, pool);

				// path = templateFile.getAbsolutePath();

			}

		}

	}

	public class OpenGLRenderer implements Renderer {

		/*
		 * 
		 * (non-Javadoc)
		 * 
		 * 
		 * 
		 * @see
		 * 
		 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.
		 * 
		 * microedition.khronos.opengles.GL10, javax.microedition.khronos.
		 * 
		 * egl.EGLConfig)
		 */

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {

			// Set the background color to black ( rgba ).

			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // OpenGL docs.

			// Enable Smooth Shading, default not really needed.

			gl.glShadeModel(GL10.GL_SMOOTH);// OpenGL docs.

			// Depth buffer setup.

			gl.glClearDepthf(1.0f);// OpenGL docs.

			// Enables depth testing.

			gl.glEnable(GL10.GL_DEPTH_TEST);// OpenGL docs.

			// The type of depth testing to do.

			gl.glDepthFunc(GL10.GL_LEQUAL);// OpenGL docs.

			// Really nice perspective calculations.

			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, // OpenGL docs.

					GL10.GL_NICEST);

		}

		/*
		 * 
		 * (non-Javadoc)
		 * 
		 * 
		 * 
		 * @see
		 * 
		 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.
		 * 
		 * microedition.khronos.opengles.GL10)
		 */

		public void onDrawFrame(GL10 gl) {

			// Clears the screen and depth buffer.

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | // OpenGL docs.

					GL10.GL_DEPTH_BUFFER_BIT);

		}

		/*
		 * 
		 * (non-Javadoc)
		 * 
		 * 
		 * 
		 * @see
		 * 
		 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.
		 * 
		 * microedition.khronos.opengles.GL10, int, int)
		 */

		public void onSurfaceChanged(GL10 gl, int width, int height) {

			// Sets the current view port to the new size.

			gl.glViewport(0, 0, width, height);// OpenGL docs.

			// Select the projection matrix

			gl.glMatrixMode(GL10.GL_PROJECTION);// OpenGL docs.

			// Reset the projection matrix

			gl.glLoadIdentity();// OpenGL docs.

			// Calculate the aspect ratio of the window

			GLU.gluPerspective(gl, 45.0f,

			(float) width / (float) height,

			0.1f, 100.0f);

			// Select the modelview matrix

			gl.glMatrixMode(GL10.GL_MODELVIEW);// OpenGL docs.

			// Reset the modelview matrix

			gl.glLoadIdentity();// OpenGL docs.

		}

	}

}