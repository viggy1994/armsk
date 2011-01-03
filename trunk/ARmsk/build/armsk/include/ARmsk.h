
#ifndef ARMSK_H_
#define ARMSK_H_

#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/video/tracking.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include "../../shared/jni/image_pool.h"
#include <vector>


namespace ARmsk
{
	using namespace cv;
    using namespace std;
	using namespace flann;
	
	struct IMAGEDATA {
		Mat image;
		vector<KeyPoint> keypoints;
		Mat descriptors;
		vector<int> validIndexes;
	};
	
	
	//Image management
	void loadImage(IMAGEDATA &input, int input_idx, image_pool* pool);
	
	//Processing
	void detectKeypoints(IMAGEDATA &input, FeatureDetector* detector);
	void computeDescriptors(IMAGEDATA &input, DescriptorExtractor* descriptorExtractor);
	//void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	void computeHomography(vector<Point2f> &pointSet1, vector<Point2f> &pointSet2, Mat &homography);
	void transformPoints(Mat &HMatrix, vector<Point2f> &srcPoints, vector<Point2f> &dstPoints);
	void estimatePose(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints, double (&_dc)[5],
					  double (&_cm)[9], double (&_d)[9], Mat &rvec, Mat &tvec, Vec3d &eav);
	
	//Convertions
	void convertToPoints(IMAGEDATA &input, vector<Point2f> &points);
	void convertToPoints2d(vector<Point2f> &pointsf, vector<Point2d> &points);
	void convertTo3D(vector<Point2f> &planarPoints, vector<Point3d> &objectPoints);
	
	//Drawing
	void drawMatchResult(int input_idx, image_pool* pool, vector<Point2f> &corners);
	void drawText(int i, image_pool* pool, const char* ctext);
	void drawFeatures(int input_idx, image_pool* pool, vector<KeyPoint> features);
	
	//Other
	void initCorners(IMAGEDATA &imageset, vector<Point2f> &corners);
	void clear(IMAGEDATA &imageset);
	

	
};

#endif
