
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


/*
 * include the headers required by the generated cpp code
 */
%{
#include "Native.h"
#include "../../../build/shared/jni/image_pool.h"
	//using namespace cv;
	using namespace ARmsk;
	//using namespace std;
	%}


//import the android-cv.i file so that swig is aware of all that has been previous defined
//notice that it is not an include....
%import "android-cv.i"

//make sure to import the image_pool as it is 
//referenced by the Processor java generated
//class
%typemap(javaimports) Native "
import com.opencv.jni.Mat;
import com.opencv.jni.image_pool;// import the image_pool interface for playing nice with
// android-opencv

/** ARmsk - for processing images that are stored in an image pool
*/"

class Native {
public:
	
	Native();
	virtual ~Native();
	
	void processAR(int input_idx, image_pool* pool);
		
	void setMarker(int input_idx, image_pool* pool);
    void setMarker(const char * filePath);
	
	float getMatrix(int i);
	float getScale();
	int getMatchFound();
	void setDrawMatchResult();
	
	
};
