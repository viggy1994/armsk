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
	void loadImage(IMAGEDATA &input, const char * filePath);
	
	//Processing
	void detectKeypoints(IMAGEDATA &input, FeatureDetector* detector);
	void computeDescriptors(IMAGEDATA &input, DescriptorExtractor* descriptorExtractor);
	//void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	void computeHomography(vector<Point2f> &pointSet1, vector<Point2f> &pointSet2, Mat &homography);
	void transformPoints(Mat &HMatrix, vector<Point2f> &srcPoints, vector<Point2f> &dstPoints);
	void prepareValuesForPoseEstimation(IMAGEDATA templateImage, Mat homography, vector<Point3d> &modelPoints, vector<Point2f> &imagePoints, Point2f &centerPoint);
	void estimatePose(vector<Point3d> modelPoints, vector<Point2f> imagePoints, Point2f centerPoint, double (cameraMatrix)[16], double (&resultMatrix)[16]);
	float calculateScale(vector<Point2f> imagePoints, float squareSize);
	
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
