#ifndef NATIVE_H_
#define NATIVE_H_

#include "ARmsk.h"

using namespace ARmsk;


class Native {

	cv::SurfFeatureDetector surfd;
	cv::SurfDescriptorExtractor surfe;

	

public:

	Native();
	virtual ~Native();
	
	void processAR(int idx, image_pool* pool, int detection_method, const char* filename);
	
	void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	
	void setTemplate(int input_idx, image_pool* pool);
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

	void convertToPoints2d(vector<Point2f> &pointsf, vector<Point2d> &points);
	void computeProjectionD(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints);

	void initCorners();
	void renderTemplateOutline(Mat H12, int input_idx, image_pool* pool);
	
	float getElementRotationVector(int i);
	float getElementTranslateVector(int i);
	float getElementRotationMatrix(int i, int j);
	
	
	void setTemplate(int input_idx, image_pool* pool);
	void loadTemplate(const char* filename);
	
	void drawText(int idx, image_pool* pool, const char* text);
	void detectAndDrawFeatures(int idx, image_pool* pool);
	
	int getNumberTemplateKeypoints();
	int getNumberTemplateDescriptors();
	float getMatrix(int i);
	float get_d(int i);
	
*/
	//void locateTemplate( const Mat& templateImage, Mat& frameImage, vector<KeyPoint>& templateKeypoints, const Mat& templateDescriptors,
			//						FeatureDetector* detector, DescriptorExtractor* descriptorExtractor,
			//						double ransacReprojThreshold, int input_idx, image_pool* pool);

	//void keypointTracker(Mat& newImage, Mat& prevImage, vector<Point2f>& prevPoints);

	//void flannFindPairs( vector<KeyPoint> templateKeypoints, Mat templateDescriptors,vector<KeyPoint> frameKeypoints, Mat frameDescriptors, vector<int> &ptpairs );
	
	
};

#endif /* ARMSK_H_ */
