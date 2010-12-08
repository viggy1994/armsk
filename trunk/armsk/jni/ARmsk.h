#ifndef ARMSK_H_
#define ARMSK_H_



#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "opencv2/video/tracking.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <vector>
#include "image_pool.h"

struct IMAGEDATA {
		   Mat image;
		   vector<KeyPoint> keypoints;
		   Mat descriptors;
		   vector<int> validIndexes;
	};


class ARmsk {
	
	cv::FastFeatureDetector fastd;
	cv::GoodFeaturesToTrackDetector gfttd;
	cv::StarFeatureDetector stard;
	cv::SurfFeatureDetector surfd;
	cv::SurfDescriptorExtractor surfe;
	cv::SiftDescriptorExtractor sifte;
	//cv::BruteForceMatcher bfm;
	
	std::vector<cv::KeyPoint> keypoints, template_keypoints;
	std::vector<cv::KeyPoint> object_keypoints, image_keypoints;
	
	cv::Mat object_descriptors,image_descriptors;
	
	cv::Mat descriptors_template;
	


	
	//image_pool pool;
public:

	ARmsk();
	virtual ~ARmsk();
	
	void processAR(int idx, image_pool* pool, int detection_method, const char* filename);
	
	

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

	float get_d(int i);
	

	//void locateTemplate( const Mat& templateImage, Mat& frameImage, vector<KeyPoint>& templateKeypoints, const Mat& templateDescriptors,
			//						FeatureDetector* detector, DescriptorExtractor* descriptorExtractor,
			//						double ransacReprojThreshold, int input_idx, image_pool* pool);

	//void keypointTracker(Mat& newImage, Mat& prevImage, vector<Point2f>& prevPoints);

	//void flannFindPairs( vector<KeyPoint> templateKeypoints, Mat templateDescriptors,vector<KeyPoint> frameKeypoints, Mat frameDescriptors, vector<int> &ptpairs );
	
	
};

#endif /* ARMSK_H_ */
