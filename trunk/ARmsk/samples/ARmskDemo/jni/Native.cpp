#include "Native.h"

#include <sys/stat.h>
#include <iostream>
#include <fstream>

bool templateInitiated = false;



Native::Native() :

surfd(400/*hessian_threshold*/, 3/*octaves*/, 4/*octave_layers*/),
surfe(3/*_nOctaves*/, 4/*_nOctaveLayers*/, true/*extended*/)

{}

IMAGEDATA train;
IMAGEDATA query;

// CAMERA PARAMETERS //

// Distortion coefficients
double _dc[] = {7.6939605311675999e-02, -5.1658229003552207e-02, 2.0525155795511341e-03, -2.5280497329674624e-03, -1.9178619077287520e-01};

// Camera matrix
double _cm[9] = {3.1810194786433851e+02, 0, 2.0127743042733985e+02, 0, 3.1880182087777166e+02, 1.2606640793496385e+02, 0, 0, 1};

ofstream myFile;

Mat HMatrix;

vector<int> matches;
vector<Point2f> templatePoints;
vector<Point2f> framePoints;

vector<Point2f> src_corners;
vector<Point2f> dst_corners;
Mat src_corners_t;
Mat objPM;


vector<double> rv(3), tv(3);

Mat rvec(rv), tvec(tv);
double _d[9] = {1,0,0,0,1,0,0,0,1};

Vec3d eav;

Native::~Native() {
	// TODO Auto-generated destructor stub
}

void Native::processAR(int input_idx, image_pool* pool) { //, int detection_method, const char* filename) {
	
	if(templateInitiated){
		
		FeatureDetector* detector = &surfd;
		DescriptorExtractor* descriptorExtractor = &surfe;
		
		loadImage(query, input_idx, pool);
		detectKeypoints(query, detector);
		computeDescriptors(query, descriptorExtractor);
		
		match(train, query, matches); 
		convertToPoints(train,templatePoints);
		convertToPoints(query,framePoints);	
		computeHomography(templatePoints,framePoints, HMatrix);
		
		vector<Point2f> imagePoints;
		transformPoints(HMatrix, src_corners, imagePoints);
		drawMatchResult(input_idx,pool, imagePoints);
		
		vector<Point3d> objectPoints;
		convertTo3D(src_corners, objectPoints);	
		
		estimatePose(objectPoints, imagePoints, _dc, _cm, _d, rvec, tvec, eav);
		
		//DEBUGGING
		
		/*myFile.open("/sdcard/test.txt");
		 
		 myFile << "Object points" << endl;
		 for(int i = 0; i < objectPoints.size(); i++){
		 myFile << " " << objectPoints[i].x << " - " << objectPoints[i].y << " - " << objectPoints[i].z << endl;
		 }
		 myFile << endl;
		 myFile << "Image points" << endl;
		 for(int i = 0; i < imagePoints.size(); i++){
		 myFile << " " << imagePoints[i].x << " - " << imagePoints[i].y << endl;
		 }
		 myFile << endl;
		 myFile << "Rotation vector" << endl;
		 for(int i = 0; i < rvec.rows; i++){
		 myFile << " " << rvec.at<float>(i,0) << endl;
		 }
		 myFile << endl;
		 myFile << "Translation vector" << endl;
		 
		 for(int i = 0; i < tvec.rows; i++){
		 myFile << " " << tvec.at<float>(i,0) << endl;
		 }
		 myFile << endl;
		 myFile << "Rotation matrix" << endl;
		 for(int i = 0; i < rMat.rows; i++){
		 for(int j = 0; j < rMat.cols; j++){
		 myFile << " " << rMat.at<float>(i,j) << " ";
		 }
		 myFile << endl;
		 }
		 
		 myFile << endl;
		 myFile << "Euler Angles" << endl;
		 for(int i = 0; i < 3; i++){
		 myFile << " " << eav[i] << " ";
		 }
		 myFile << endl;
		 
		 
		 myFile << endl;
		 myFile << "Homography matrix" << endl;
		 for(int i = 0; i < HMatrix.rows; i++){
		 for(int j = 0; j < HMatrix.cols; j++){
		 myFile << " " << HMatrix.at<float>(i,j) << " ";
		 }
		 myFile << endl;
		 }
		 */
		
		clearAll();
		//templateInitiated = false;
	}
	
	
}

void Native::clearAll(){
	matches.clear();
	templatePoints.clear();
	framePoints.clear();
	dst_corners.clear();
	clear(query);
	
}

void Native::match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches){
	
	// find nearest neighbors using FLANN
	cv::Mat m_indices(trainData.descriptors.rows, 2, CV_32S);
	cv::Mat m_dists(trainData.descriptors.rows, 2, CV_32F);
	
	// Using 4 randomized kdtrees
	cv::flann::Index flann_index(queryData.descriptors, cv::flann::KDTreeIndexParams(4));
	// Maximum number of leafs checked
	flann_index.knnSearch(trainData.descriptors, m_indices, m_dists, 2, cv::flann::SearchParams(64));
	
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

void Native::setMarker(const char * filePath ) {
	
	FeatureDetector* detector = &surfd;
	DescriptorExtractor* descriptorExtractor = &surfe;
	
	loadImage(train, filePath);
	detectKeypoints(train, detector);
	computeDescriptors(train, descriptorExtractor);
	
	initCorners(train, src_corners);
	templateInitiated = true;
}

void Native::saveMarker(int input_idx, image_pool* pool, const char * filePath ){
	Mat* img = pool->getImage(input_idx);
	imwrite(filePath, *img);
}
/*
 void ARmsk::loadTemplate(const char* filename) {
 
 Mat templateRaw = imread(filename);
 cvtColor(templateRaw, train.image, CV_RGB2GRAY);
 
 detectKeypoints(train);
 computeDescriptors(train);
 
 initCorners();
 templateInitiated = true;
 }
 
 
 /*
 int ARmsk::getNumberTemplateKeypoints() {
 return train.keypoints.size();
 }
 
 int ARmsk::getNumberTemplateDescriptors() {
 return train.descriptors.rows;
 }
 
 
 
 
 float ARmsk::getElementRotationVector(int i){
 return rvec.at<float>(i,0);
 }
 
 float ARmsk::getElementTranslateVector(int i){
 return tvec.at<float>(i,0);
 
 }
 
 float ARmsk::getElementRotationMatrix(int i, int j){
 return rMat.at<float>(i,j);
 }
 
 float ARmsk::get_d(int i){
 return _d[i];
 
 }
 
 
 
 */

float Native::getMatrix(int i) {
	return eav[i];
}
