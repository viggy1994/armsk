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
	
	void prepareValuesForPoseEstimation(IMAGEDATA templateImage, Mat homography, vector<Point3d> &modelPoints, vector<Point2f> &imagePoints, Point2f &centerPoint ){
		
		float height = templateImage.image.rows;
		float width = templateImage.image.cols;
		
		modelPoints.clear();
		modelPoints.push_back(Point3d(0.0, 0.0, 0.0));
		modelPoints.push_back(Point3d(height, 0.0, 0.0));
		modelPoints.push_back(Point3d(height, height, 0.0));
		modelPoints.push_back(Point3d(0.0, height, 0.0));
		
		vector<Point2f> squareCorners;
		squareCorners.push_back(Point2f(((width / 2.0f) - (height / 2.0f)), 0.0f));
		squareCorners.push_back(Point2f(((width / 2.0f) + (height / 2.0f)), 0.0f));
		squareCorners.push_back(Point2f(((width / 2.0f) + (height / 2.0f)), height));
		squareCorners.push_back(Point2f(((width / 2.0f) - (height / 2.0f)), height));
		
		imagePoints.clear();
		transformPoints(homography, squareCorners, imagePoints);
		
		vector<Point2f> centerPointTemplate;
		vector<Point2f> temp;
		centerPointTemplate.push_back(Point2f(width/2.0f, height/2.0f));
		transformPoints(homography, centerPointTemplate, temp);
		
		centerPoint = temp[0];
		
	}	
	void estimatePose(vector<Point3d> modelPoints, vector<Point2f> imagePoints, Point2f centerPoint, double (cameraMatrix)[16], double (&resultMatrix)[16] ){
		
		Mat rvec, tvec, rMat;
		
		double rot[9] = { 0 };
		vector<double> rv(3), tv(3);
		
		rvec = Mat(rv);
		
		double _d[9] = { 1, 0, 0, 0, -1, 0, 0, 0, -1 };
		
		
		Rodrigues(Mat(3, 3, CV_64FC1, _d), rvec);
		
		tv[0] = 0;
		tv[1] = 0;
		tv[2] = 1;
		
		tvec = Mat(tv);
		
		
		// Camera matrix
		double _cm[9] = { 3.1810194786433851e+02, 0., 2.0127743042733985e+02, 0.,
			3.1880182087777166e+02, 1.2606640793496385e+02, 0., 0., 1 };
		
		// Distortion coefficients
		double _dc[] = { 0, 0, 0, 0 };
		
		
		Mat camMatrix = Mat(3, 3, CV_64FC1, _cm);
		
		solvePnP(Mat(modelPoints), Mat(imagePoints), camMatrix, Mat(1, 4,
																	CV_64FC1, _dc), rvec, tvec, true);
		
		
		rMat = Mat(3, 3, CV_64FC1, rot);
		
		Rodrigues(rvec, rMat);
		
		tvec.at<double> (0, 0) += 2.0127743042733985e+02 + 4.69099e-02;
		tvec.at<double> (1, 0) += 1.2606640793496385e+02 - 2.5968e-02;
		tvec.at<double> (2, 0) -= 3.1810194786433851e+02 + 2.31789e-01;
		
		
		
		resultMatrix[0] = rMat.at<double> (0, 0);//*scale;
		resultMatrix[1] = -rMat.at<double> (1, 0);
		resultMatrix[2] = -rMat.at<double> (2, 0);
		resultMatrix[3] = 0;
		resultMatrix[4] = rMat.at<double> (0, 1);
		resultMatrix[5] = -rMat.at<double> (1, 1);//*scale;
		resultMatrix[6] = -rMat.at<double> (2, 1);
		resultMatrix[7] = 0;
		resultMatrix[8] = rMat.at<double> (0, 2);
		resultMatrix[9] = -rMat.at<double> (1, 2);
		resultMatrix[10] = -rMat.at<double> (2, 2);//*scale;
		resultMatrix[11] = 0;
		resultMatrix[12] = 4.0f*(4.0f/3.0f)*(5.0f/3.0f)*(centerPoint.x - 200)/400;
		resultMatrix[13] = -4.0f*(4.0f/3.0f)*1.0f*(centerPoint.y - 120)/240;
		resultMatrix[14] = -4.0f;
		resultMatrix[15] = 1.0f;
		
	}	
	
	float calculateScale(vector<Point2f> imagePoints, float squareSize){
		vector<float> hypotenuse;
		hypotenuse.push_back(sqrt((imagePoints[1].x - imagePoints[0].x)*(imagePoints[1].x - imagePoints[0].x) + (imagePoints[1].y - imagePoints[0].y)*(imagePoints[1].y - imagePoints[0].y)));
		hypotenuse.push_back(sqrt((imagePoints[2].x - imagePoints[1].x)*(imagePoints[2].x - imagePoints[1].x) + (imagePoints[2].y - imagePoints[1].y)*(imagePoints[2].y - imagePoints[1].y)));
		hypotenuse.push_back(sqrt((imagePoints[3].x - imagePoints[2].x)*(imagePoints[3].x - imagePoints[2].x) + (imagePoints[3].y - imagePoints[2].y)*(imagePoints[3].y - imagePoints[2].y)));
		hypotenuse.push_back(sqrt((imagePoints[0].x - imagePoints[3].x)*(imagePoints[0].x - imagePoints[3].x) + (imagePoints[0].y - imagePoints[3].y)*(imagePoints[0].y - imagePoints[3].y)));
		sort (hypotenuse.begin(),hypotenuse.end());
		return hypotenuse[3]/squareSize;
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