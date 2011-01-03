
#include "../include/ARmsk.h"

namespace ARmsk
{
	
	void loadImage(IMAGEDATA &input, int input_idx, image_pool* pool){
		pool->getGrey(input_idx, input.image);
	}
	
	void loadImage(IMAGEDATA &input, const char * filePath){
		Mat imageFile = imread(filePath);
		cvtColor(imageFile, input.image, CV_RGB2GRAY);
	}
	
	void detectKeypoints(IMAGEDATA &input, FeatureDetector* detector){
		detector->detect(input.image, input.keypoints);
	}
	
	void computeDescriptors(IMAGEDATA &input, DescriptorExtractor* descriptorExtractor ){
		if(!input.keypoints.empty())
			descriptorExtractor->compute(input.image, input.keypoints, input.descriptors);
	}
/*
	void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches){
		
		// find nearest neighbors using FLANN
		Mat m_indices(trainData.descriptors.rows, 2, CV_32S);
		Mat m_dists(trainData.descriptors.rows, 2, CV_32F);
		
		// Using 4 randomized kdtrees
		Index flann_index(queryData.descriptors, KDTreeIndexParams(4));
		// Maximum number of leafs checked
		flann_index.knnSearch(trainData.descriptors, m_indices, m_dists, 2, SearchParams(64));
		
		int* indices_ptr = m_indices.ptr<int> (0);
		float* dists_ptr = m_dists.ptr<float> (0);
		for (int i = 0; i < m_indices.rows; ++i) {
			if (dists_ptr[2 * i] < 0.6 * dists_ptr[2 * i + 1]) {
				matches.push_back(i);
				trainData.validIndexes.push_back(i);
				matches.push_back(indices_ptr[2 * i]);
				queryData.validIndexes.push_back(indices_ptr[2 * i]);
			}
		}
	}*/
 
	
	void computeHomography(vector<Point2f> &pointSet1, vector<Point2f> &pointSet2, Mat &homography){
		homography = findHomography(Mat(pointSet1), Mat(pointSet2), CV_RANSAC, 10);
	}
	
	void transformPoints(Mat &HMatrix, vector<Point2f> &srcPoints, vector<Point2f> &dstPoints ){
		Mat transformedPoints;
		perspectiveTransform(Mat(srcPoints), transformedPoints, HMatrix);
		
		for (size_t i = 0; i < srcPoints.size(); i++)
			dstPoints.push_back(transformedPoints.at<Point2f> (i, 0));
	}
	
	void estimatePose(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints, double (&_dc)[5], double (&_cm)[9], double (&_d)[9], Mat &rvec, Mat &tvec, Vec3d &eav){
		
		Mat distC = Mat(1,4,CV_64FC1,_dc);
		Mat camMatrix = Mat(3,3,CV_64FC1,_cm);
		
		
		Mat _objectPoints;
		Mat(objectPoints).convertTo(_objectPoints,CV_64F);
		
		Mat rotM(3,3,CV_64FC1,_d);
		Rodrigues(rotM,rvec);
		
		solvePnP(_objectPoints,Mat(imagePoints),camMatrix,distC,rvec,tvec,true);
		
		Rodrigues(rvec,rotM);
		
		double _pm[12] = {_d[0], _d[1], _d[2], 0,
			_d[3], _d[4], _d[5],  0,
			_d[6], _d[7], _d[8], 0};
		
		Mat tmp, tmp2, tmp3, tmp4, tmp5, tmp6;
		decomposeProjectionMatrix(Mat(3, 4, CV_64FC1, _pm),tmp, tmp2, tmp3, tmp4, tmp5, tmp6, eav);
		
	}
	
	void convertToPoints(IMAGEDATA &input, vector<Point2f> &points){
		KeyPoint::convert(input.keypoints, points, input.validIndexes);
	}
	
	void convertToPoints2d(vector<Point2f> &pointsf, vector<Point2d> &points){
		for(int i = 0; i < pointsf.size(); i++)
			points.push_back(Point2d(pointsf[i].x, pointsf[i].y));
	}
	
	void convertTo3D(vector<Point2f> &planarPoints, vector<Point3d> &objectPoints){
		for(int i = 0; i < planarPoints.size(); i++)
			objectPoints.push_back(Point3d(planarPoints[i].x, planarPoints[i].y, 0.0f));
		
	}
	
	void initCorners(IMAGEDATA &imageset, vector<Point2f> &corners){
		corners.push_back(Point2f(0.0f, 0.0f));
		corners.push_back(Point2f(imageset.image.cols, 0.0f));
		corners.push_back(Point2f(imageset.image.cols, imageset.image.rows));
		corners.push_back(Point2f(0.0f, imageset.image.rows));
	}
	
	void clear(IMAGEDATA &imageset){
		vector<int> empty(2);
		empty[0] = 0; empty[1] = 0;  
		imageset.image = Mat(empty);
		imageset.keypoints.clear();
		imageset.descriptors = Mat(empty);
		imageset.validIndexes.clear();
	}
	
	void drawMatchResult(int input_idx, image_pool* pool, vector<Point2f> &corners) {
		
		Mat * img = pool->getImage(input_idx);
		
		for (int i = 0; i < 4; i++) {
			Point2f r1 = corners[i % 4];
			Point2f r2 = corners[(i + 1) % 4];
			line(*img, Point2f(r1.x, r1.y), Point2f(r2.x, r2.y),
				 CV_RGB(0, 128, 255), 2);
		}
		
	}
	
	void drawText(int i, image_pool* pool, const char* ctext) {
		// Use "y" to show that the baseLine is about
		string text = ctext;
		int fontFace = FONT_HERSHEY_COMPLEX_SMALL;
		double fontScale = .8;
		int thickness = .5;
		
		Mat img = *pool->getImage(i);
		
		int baseline = 0;
		Size textSize =
		getTextSize(text, fontFace, fontScale, thickness, &baseline);
		baseline += thickness;
		
		// center the text
		Point textOrg((img.cols - textSize.width) / 2, (img.rows - textSize.height
														* 2));
		
		// draw the box
		rectangle(img, textOrg + Point(0, baseline), textOrg + Point(
																	 textSize.width, -textSize.height), Scalar(0, 0, 255), CV_FILLED);
		// ... and the baseline first
		line(img, textOrg + Point(0, thickness), textOrg + Point(textSize.width,
																 thickness), Scalar(0, 0, 255));
		
		// then put the text itself
		putText(img, text, textOrg, fontFace, fontScale, Scalar::all(255),
				thickness, 8);
		
	}
	
	void drawFeatures(int input_idx, image_pool* pool, vector<KeyPoint> features) {
		
		Mat greyimage;
		pool->getGrey(input_idx, greyimage);
		
		Mat* img = pool->getImage(input_idx);
		
		if (!img || greyimage.empty())
			return; //no image at input_idx!
		
		for (vector<KeyPoint>::const_iterator it = features.begin(); it
			 != features.end(); ++it) {
			circle(*img, it->pt, 3, cvScalar(0, 0, 255, 0));
			
		}
	}
	
	

	
}