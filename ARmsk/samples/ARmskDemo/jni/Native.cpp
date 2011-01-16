
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

#include "Native.h"

bool templateInitiated = false;

Native::Native() :

	surfd(400/*hessian_threshold*/, 3/*octaves*/, 4/*octave_layers*/),
	surfe(3/*_nOctaves*/, 4/*_nOctaveLayers*/, true/*extended*/)

{
}

/* CAMERA PARAMETERS FOR OUR HTC DESIRE
 * These will be set dynamically in later releases*/

// Distortion coefficients
double _dc[] = { 7.6939605311675999e-02, -5.1658229003552207e-02,
		2.0525155795511341e-03, -2.5280497329674624e-03,
		-1.9178619077287520e-01 };

// Camera matrix
double cameraMatrix[9] = { 3.1810194786433851e+02, 0, 2.0127743042733985e+02, 0,
		3.1880182087777166e+02, 1.2606640793496385e+02, 0, 0, 1 };


IMAGEDATA train;
IMAGEDATA query;


Mat HMatrix;

vector<int> matches;
vector<Point2f> templatePoints;
vector<Point2f> framePoints;

vector<Point2f> src_corners;
vector<Point2f> dst_corners;


double transformationMatrix[16];
float scale = 1.0f;
bool drawMatchFound = true;
bool matchFound;



Native::~Native() {
	// TODO Auto-generated destructor stub
}

void Native::processAR(int input_idx, image_pool* pool) {

	matchFound = false;

	FeatureDetector* detector = &surfd;
	DescriptorExtractor* descriptorExtractor = &surfe;

	//Load and find features for the camera stream image
	loadImage(query, input_idx, pool);
	detectKeypoints(query, detector);
	computeDescriptors(query, descriptorExtractor);

	//Match train (image marker) descriptors with query (camera stream) descriptors
	match(train, query, matches);

	//Find the homography for the matched points
	convertToPoints(train, templatePoints);
	convertToPoints(query, framePoints);
	computeHomography(templatePoints, framePoints, HMatrix);

	vector<Point2f> imagePoints;
	transformPoints(HMatrix, src_corners, imagePoints);

	if (((double) matches.size() * 0.5 / (double) query.keypoints.size())> 0.1) {

		matchFound = true;
		drawMatchResult(input_idx, pool, imagePoints);

	}


	if(matchFound){

	vector<Point3d> modelPoints;
	vector<Point2f> imagePoints;
	Point2f centerPoint;

	prepareValuesForPoseEstimation(train, HMatrix, modelPoints, imagePoints, centerPoint);
	estimatePose(modelPoints,imagePoints,centerPoint,cameraMatrix, transformationMatrix);

	scale = calculateScale(imagePoints, 240.0f);

	}

}

void Native::clearAll() {
	matches.clear();
	templatePoints.clear();
	framePoints.clear();
	dst_corners.clear();
	clear( query);

}

/* This method will be moved into the ARmsk library in next release */
void Native::match(IMAGEDATA &trainData, IMAGEDATA &queryData,
		vector<int> &matches) {

	// find nearest neighbors using FLANN
	cv::Mat m_indices(trainData.descriptors.rows, 2, CV_32S);
	cv::Mat m_dists(trainData.descriptors.rows, 2, CV_32F);

	// Using 4 randomized kdtrees
	cv::flann::Index flann_index(queryData.descriptors,
			cv::flann::KDTreeIndexParams(4));

	// Maximum number of leafs checked
	flann_index.knnSearch(trainData.descriptors, m_indices, m_dists, 2,
			cv::flann::SearchParams(64));

	trainData.validIndexes.clear();
	queryData.validIndexes.clear();

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
}

void Native::setMarker(int input_idx, image_pool* pool) {

	FeatureDetector* detector = &surfd;
	DescriptorExtractor* descriptorExtractor = &surfe;

	loadImage(train, input_idx, pool);
	detectKeypoints(train, detector);
	computeDescriptors(train, descriptorExtractor);

	initCorners(train, src_corners);
	templateInitiated = true;
}

void Native::setMarker(const char * filePath) {

	FeatureDetector* detector = &surfd;
	DescriptorExtractor* descriptorExtractor = &surfe;

	loadImage(train, filePath);
	detectKeypoints(train, detector);
	computeDescriptors(train, descriptorExtractor);

	initCorners(train, src_corners);
	templateInitiated = true;
}

/*This method will be changed to take a array reference input for better performance*/
float Native::getMatrix(int i) {
	return transformationMatrix[i];
}

float Native::getScale() {
	return scale;
}

void Native::setDrawMatchResult() {
	if(drawMatchResult)
		drawMatchFound = false;
	else
		drawMatchFound = true;
}

int Native::getMatchFound() {
	int temp;
	if (matchFound)
		temp = 1;
	else
		temp = 0;
	return temp;
}


/* These methods will be used in later versions

 void Native::saveMarker(int input_idx, image_pool* pool, const char * filePath) {
	Mat* img = pool->getImage(input_idx);
	imwrite(filePath, *img);
}

void Native::findFeatures(const char * filePath, const char * dstPath) {
	FeatureDetector* detector = &surfd;

	Mat templateRaw = imread(filePath);
	Mat greyimage;
	cvtColor(templateRaw, greyimage, CV_RGB2GRAY);

	//vector<KeyPoint> features;
	detector->detect(greyimage, temp.keypoints);

	for (vector<KeyPoint>::const_iterator it = temp.keypoints.begin(); it
			!= temp.keypoints.end(); ++it) {
		circle(templateRaw, it->pt, 3, cvScalar(0, 0, 255, 0));

	}
	imwrite(dstPath, templateRaw);
}

int Native::getNumberFeatures() {
	return temp.keypoints.size();
}
*/
