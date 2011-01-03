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
	
	
	//void processAR(int input_idx, image_pool* pool, int detection_method,const char* filename);
	void processAR(int input_idx, image_pool* pool);
	
	void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	
	//void initCorners();
	//void renderTemplateOutline(Mat H12, int input_idx, image_pool* pool);
	
	void setMarker(int input_idx, image_pool* pool);
    void setMarker(const char * filePath);
	
	void saveMarker(int input_idx, image_pool* pool, const char * filePath );
	void clearAll();
	
	//void estimatePose(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints);
	//void estimatePose(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints, Mat distC, Mat cameraM, Mat &rotM, Mat &rvec, Mat &tvec, Vec3d &eav);

	float getMatrix(int i);
	/*
	void loadImage(IMAGEDATA &input, int input_idx, image_pool* pool);
	void detectKeypoints(IMAGEDATA &input);
	void computeDescriptors(IMAGEDATA &input);
	void locateTemplate(IMAGEDATA &templateData, IMAGEDATA &frameData);
	void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	void convertToPoints(IMAGEDATA &input, vector<Point2f> &points);
	void estimatePose(vector<Point2f> &pointSet1, vector<Point2f> &pointSet2, Mat &homography);
	void clearAll();
	void transformPoints(Mat &HMatrix, vector<Point2f> &srcPoints, vector<Point2f> &dstPoints );
	void convertTo3D(vector<Point2f> &planarPoints, vector<Point3d> &objectPoints);
	void computeProjection(vector<Point3f> &objectPoints, vector<Point2f> &imagePoints);
	
	
	void computeProjectionD(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints);
	void convertToPoints2d(vector<Point2f> &pointsf, vector<Point2d> &points);
	
	float getElementRotationVector(int i);
	float getElementTranslateVector(int i);
	float getElementRotationMatrix(int i, int j);
	
	
	void renderTemplateOutline(Mat H12, int input_idx, image_pool* pool);
	
	
	
	void drawText(int idx, image_pool* pool, const char* text);
	
	void setTemplate(int input_idx, image_pool* pool);
	void loadTemplate(const char* filename);
	
	void detectAndDrawFeatures(int idx, image_pool* pool);
	
	int	getNumberTemplateKeypoints();
	int getNumberTemplateDescriptors();
	void initCorners();
	*/
	/*
	void locateTemplate( const Mat& templateImage, Mat& frameImage,
						vector<KeyPoint>& templateKeypoints, const Mat& templateDescriptors,
						FeatureDetector* detector, DescriptorExtractor* descriptorExtractor,
						double ransacReprojThreshold, int input_idx, image_pool* pool);
	
	void keypointTracker(Mat& newImage, Mat& prevImage, vector<Point2f>& prevPoints);
	 
	void flannFindPairs( vector<KeyPoint> templateKeypoints, Mat templateDescriptors,
	vector<KeyPoint> frameKeypoints, Mat frameDescriptors, vector<int> &ptpairs );
	*/
	
	//float get_d(int i);
	//float getMatrix(int i);
	
	
	
};
