#include "ARmsk.h"

#include <sys/stat.h>
#include <iostream>
#include <fstream>

using namespace cv;
using namespace std;

bool templateInitiated = false;



ARmsk::ARmsk() :
fastd(20/*threshold*/, true/*nonmax_suppression*/), 
gfttd(200/*maxCorners*/, 0.01/*qualityLevel*/, 0.07/*blockSize*/, 3,false/*useHarrisDetector*/, 0/*if_Harris_k*/),
stard(20/*max_size*/, 8/*response_threshold*/,15/*line_threshold_projected*/,8/*line_threshold_binarized*/, 5/*suppress_nonmax_size*/),
surfd(400/*hessian_threshold*/, 3/*octaves*/, 4/*octave_layers*/),
surfe(3/*_nOctaves*/, 4/*_nOctaveLayers*/, true/*extended*/)

{
	
}

IMAGEDATA train;
IMAGEDATA query;

ofstream myFile;

Mat HMatrix;

vector<int> matches;
vector<Point2f> templatePoints;
vector<Point2f> framePoints;

// Outline variables
vector<Point2f> src_corners;
vector<Point2f> dst_corners;
Mat src_corners_t;
Mat objPM;

vector<double> rv(3), tv(3);

Mat rvec(rv), tvec(tv);
double _d[9] = {1,0,0,0,1,0,0,0,1};
Mat rMat(3,3,CV_64FC1,_d);;

ARmsk::~ARmsk() {
	// TODO Auto-generated destructor stub
}

void ARmsk::processAR(int input_idx, image_pool* pool, int detection_method, const char* filename) {
	
	if(templateInitiated){

		loadImage(query, input_idx, pool);
		detectKeypoints(query);
		computeDescriptors(query);

		locateTemplate(train, query);

		renderTemplateOutline(HMatrix,input_idx,pool);

		vector<Point2f> imagePoints;

		transformPoints(HMatrix, src_corners, imagePoints);
		//vector<Point2d> imagePointsDouble;
		//convertToPoints2d(imagePoints,imagePointsDouble);

		vector<Point3d> templatePoints3D;
		convertTo3D(src_corners,templatePoints3D);
		//computeProjection(templatePoints3D, templatePointsTransformed);
		computeProjectionD(templatePoints3D, imagePoints);

		myFile.open("/sdcard/test.txt");

		myFile << "Object points" << endl;
		for(int i = 0; i < templatePoints3D.size(); i++){
			myFile << " " << templatePoints3D[i].x << " - " << templatePoints3D[i].y << " - " << templatePoints3D[i].z << endl;
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
		myFile << "Homography matrix" << endl;
		for(int i = 0; i < HMatrix.rows; i++){
			for(int j = 0; j < HMatrix.cols; j++){
				myFile << " " << HMatrix.at<float>(i,j) << " ";
			}
			myFile << endl;
		}

		myFile.close();

		//templateInitiated = false;

		clearAll();
		imagePoints.clear();
		templatePoints3D.clear();
	}
	
}

void ARmsk::loadImage(IMAGEDATA &input, int input_idx, image_pool* pool){
	pool->getGrey(input_idx, input.image);
}

void ARmsk::detectKeypoints(IMAGEDATA &input){
	FeatureDetector* detector = &surfd;
	detector->detect(input.image, input.keypoints);
}

void ARmsk::computeDescriptors(IMAGEDATA &input){
	DescriptorExtractor* descriptorExtractor = &surfe;
	if(!input.keypoints.empty())
	descriptorExtractor->compute(input.image, input.keypoints, input.descriptors);
}


void ARmsk::locateTemplate(IMAGEDATA &templateData, IMAGEDATA &frameData){

	match(train,query,matches);

	convertToPoints(train,templatePoints);
	convertToPoints(query,framePoints);

	estimatePose(templatePoints,framePoints, HMatrix);

}

void ARmsk::clearAll(){
	matches.clear();
	templatePoints.clear();
	framePoints.clear();
	train.validIndexes.clear();
	query.validIndexes.clear();
	dst_corners.clear();

}

void ARmsk::match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches){

	// find nearest neighbors using FLANN
	cv::Mat m_indices(trainData.descriptors.rows, 2, CV_32S);
	cv::Mat m_dists(trainData.descriptors.rows, 2, CV_32F);

	// Using 4 randomized kdtrees
	cv::flann::Index flann_index(queryData.descriptors, cv::flann::KDTreeIndexParams(4));
	// Maximum number of leafs checked
	flann_index.knnSearch(trainData.descriptors, m_indices, m_dists, 2, cv::flann::SearchParams(64));

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

void ARmsk::convertToPoints(IMAGEDATA &input, vector<Point2f> &points){
	KeyPoint::convert(input.keypoints, points, input.validIndexes);

}

void ARmsk::convertToPoints2d(vector<Point2f> &pointsf, vector<Point2d> &points){

	for(int i = 0; i < pointsf.size(); i++){
		points.push_back(Point2d(pointsf[i].x, pointsf[i].y));
	}
}

void ARmsk::estimatePose(vector<Point2f> &pointSet1, vector<Point2f> &pointSet2, Mat &homography){
	homography = findHomography(Mat(pointSet1), Mat(pointSet2), CV_RANSAC, 10);
}

void ARmsk::setTemplate(int input_idx, image_pool* pool) {
	loadImage(train, input_idx, pool);
	detectKeypoints(train);
	computeDescriptors(train);

	initCorners();
	templateInitiated = true;
}

void ARmsk::loadTemplate(const char* filename) {

	Mat templateRaw = imread(filename);
	cvtColor(templateRaw, train.image, CV_RGB2GRAY);

	detectKeypoints(train);
	computeDescriptors(train);

	initCorners();
	templateInitiated = true;
}

void ARmsk::transformPoints(Mat &HMatrix, vector<Point2f> &srcPoints, vector<Point2f> &dstPoints ){
	Mat transformedPoints;
	perspectiveTransform(Mat(srcPoints), transformedPoints, HMatrix);

	for (size_t i = 0; i < srcPoints.size(); i++) {
		dstPoints.push_back(transformedPoints.at<Point2f> (i, 0));
	}
}

void ARmsk::convertTo3D(vector<Point2f> &planarPoints, vector<Point3d> &objectPoints){

for(int i = 0; i < planarPoints.size(); i++){
	objectPoints.push_back(Point3d(planarPoints[i].x, planarPoints[i].y, 0.0));
}
}

void ARmsk::computeProjection(vector<Point3f> &objectPoints, vector<Point2f> &imagePoints){

	//Mat op;
	//Mat(objectPoints).convertTo(op, CV_32F);
/*
	vector<Point3f> test;

	test.push_back(Point3f(0.0f,0.0f,0.0f));
	test.push_back(Point3f(train.image.cols,0.0f,0.0f));
	test.push_back(Point3f(train.image.cols,train.image.rows,0.0f));
	test.push_back(Point3f(0.0f,train.image.rows,0.0f));

	Mat transformed_corners;
	perspectiveTransform(Mat(src_corners), transformed_corners, HMatrix);

	for (size_t i = 0; i < 4; i++) {
			dst_corners.push_back(transformed_corners.at<Point2f> (i, 0));
	}*/

	/*

	vector<Point2f> test2;
	for(int j = 0; j < 4; j++){
	Point2f r1 = dst_corners[j % 4];
	test2.push_back(Point2f(r1.x,r1.y));
	}

	*/

	//Mat op = Mat(objectPoints);
	//op = op / 400;
	double rot[9] = {0};
	vector<double> rv(3), tv(3);

	rvec = Mat(rv);

	double _d[9] = {1,0,0,0,-1,0,0,0,-1};

	Rodrigues(Mat(3,3,CV_64FC1,_d),rvec);

	tv[0]=0;tv[1]=0;tv[2]=1;

	tvec = Mat(tv);


	double _dc[] = {0,0,0,0};

	double _cm[9] = { 20, 0, 400,
		           0, 20, 240,
		             0,  0,   1 };

	Mat camMatrix = Mat(3,3,CV_64FC1,_cm);


	solvePnP(Mat(objectPoints),Mat(imagePoints),camMatrix,Mat(1,4,CV_64FC1,_dc),rvec,tvec,true);

	Rodrigues(rvec,rMat);

}

void ARmsk::computeProjectionD(vector<Point3d> &objectPoints, vector<Point2f> &imagePoints){

	//double rot[9] = {0};

	Mat(objectPoints).convertTo(objPM,CV_32F);


	tv[0]=0;tv[1]=0;tv[2]=1;

	//tvec = Mat(tv);


	double _dc[] = {0,0,0,0};

	double _cm[9] = { 20, 0, 400,
		           0, 20, 240,
		             0,  0,   1 };

	Mat camMatrix = Mat(3,3,CV_64FC1,_cm);

	Rodrigues(rMat,rvec);

	solvePnP(objPM,Mat(imagePoints),camMatrix,Mat(1,4,CV_64FC1,_dc),rvec,tvec,true);

	Rodrigues(rvec,rMat);

}


void ARmsk::renderTemplateOutline(Mat H12, int input_idx, image_pool* pool) {
	
	// Transform the initial template corners with the transformation matrix H12
	perspectiveTransform(Mat(src_corners), src_corners_t, H12);
	
	for (size_t i = 0; i < 5; i++) {
		dst_corners.push_back(src_corners_t.at<Point2f> (i, 0));
	}
	
	Mat * img = pool->getImage(input_idx);
	
	// Draw lines between each corner
	for (int i = 0; i < 4; i++) {
		Point2f r1 = dst_corners[i % 4];
		Point2f r2 = dst_corners[(i + 1) % 4];
		line(*img, Point2f(r1.x, r1.y), Point2f(r2.x, r2.y),
			 CV_RGB(0, 128, 255), 2);
	}
	
	dst_corners.clear();
	
}

void ARmsk::initCorners() {
	src_corners.push_back(Point2f(0.0f, 0.0f));
	src_corners.push_back(Point2f(train.image.cols, 0.0f));
	src_corners.push_back(Point2f(train.image.cols, train.image.rows));
	src_corners.push_back(Point2f(0.0f, train.image.rows));
}

int ARmsk::getNumberTemplateKeypoints() {
	return train.keypoints.size();
}

int ARmsk::getNumberTemplateDescriptors() {
	return train.descriptors.rows;
}

void ARmsk::detectAndDrawFeatures(int input_idx, image_pool* pool) {
	FeatureDetector* fd = 0;
	
	fd = &fastd;
	
	Mat greyimage;
	pool->getGrey(input_idx, greyimage);
	//Mat* grayimage = pool->getYUV(input_idx);
	
	
	Mat* img = pool->getImage(input_idx);
	
	if (!img || greyimage.empty() || fd == 0)
		return; //no image at input_idx!
	
	
	keypoints.clear();
	
	//if(grayimage->step1() > sizeof(uchar)) return;
	//cvtColor(*img,*grayimage,CV_RGB2GRAY);
	
	
	fd->detect(greyimage, keypoints);
	
	for (vector<KeyPoint>::const_iterator it = keypoints.begin(); it
		 != keypoints.end(); ++it) {
		circle(*img, it->pt, 3, cvScalar(0, 0, 255, 0));
		
	}
	
	//pool->addImage(input_idx+1,img);
	
}

void ARmsk::drawText(int i, image_pool* pool, const char* ctext) {
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


/*
void ARmsk::locateTemplate(const Mat& templateImage, Mat& frameImage,
								   vector<KeyPoint>& templateKeypoints, const Mat& templateDescriptors,
								   FeatureDetector* detector, DescriptorExtractor* descriptorExtractor,
								   double ransacReprojThreshold, int input_idx, image_pool* pool) {

	// Detect keypoints in the frame image
	detector->detect(frameImage, frameKeypoints);
	if (!frameKeypoints.empty())
		// Extract descriptors from frame image
		descriptorExtractor->compute(frameImage, frameKeypoints,
									 frameDescriptors);

	if (!frameDescriptors.empty() && !templateDescriptors.empty()) {
		// Match template descriptors with frame descriptors using FLANN
		flannFindPairs(templateKeypoints, templateDescriptors, frameKeypoints,
					   frameDescriptors, ptpairs);

		vector<int> trainIdxs(ptpairs.size() / 2);
		vector<int> queryIdxs(ptpairs.size() / 2);
		vector<DMatch> matches(ptpairs.size() / 2);

		for (size_t j = 0; j < ptpairs.size() / 2; j++) {

			matches[j].indexQuery = queryIdxs[j] = ptpairs[j * 2 + 1];
			matches[j].indexTrain = trainIdxs[j] = ptpairs[j * 2];

		}

		if (ransacReprojThreshold >= 0) {
			// Convert the keypoints into Points2f
			KeyPoint::convert(templateKeypoints, templatePoints, trainIdxs);
			KeyPoint::convert(frameKeypoints, framePoints, queryIdxs);
			// Find the transformation matrix between template and frame points
			H12 = findHomography(Mat(templatePoints), Mat(framePoints),
								 CV_RANSAC, ransacReprojThreshold);
		}
		ptpairs.clear();
	}
}


void ARmsk::flannFindPairs(vector<KeyPoint> templateKeypoints,
							   Mat templateDescriptors, vector<KeyPoint> frameKeypoints,
							   Mat frameDescriptors, vector<int> &ptpairs) {

	// find nearest neighbors using FLANN
	cv::Mat m_indices(templateDescriptors.rows, 2, CV_32S);
	cv::Mat m_dists(templateDescriptors.rows, 2, CV_32F);

	// Using 4 randomized kdtrees
	cv::flann::Index flann_index(frameDescriptors, cv::flann::KDTreeIndexParams(4));
	// Maximum number of leafs checked
	flann_index.knnSearch(templateDescriptors, m_indices, m_dists, 2, cv::flann::SearchParams(64));


	int* indices_ptr = m_indices.ptr<int> (0);
	float* dists_ptr = m_dists.ptr<float> (0);
	for (int i = 0; i < m_indices.rows; ++i) {
		if (dists_ptr[2 * i] < 0.6 * dists_ptr[2 * i + 1]) {
			ptpairs.push_back(i);
			ptpairs.push_back(indices_ptr[2 * i]);
		}
	}

}

void ARmsk::keypointTracker(Mat& newImage, Mat& prevImage, vector<Point2f>& prevPoints){

	vector<Point2f> newPoints;
	vector<uchar> status;
	vector<float> error;
	Size winSize(15,15);
	TermCriteria criteria = TermCriteria(TermCriteria::COUNT+TermCriteria::EPS, 20, 0.3);

	calcOpticalFlowPyrLK(prevImage, newImage, prevPoints, newPoints, status, error, winSize, 3, criteria, 0.5, 0 );

	vector<Point2f> correctPoints;
	vector<Point2f> correctTemplatePoints = templatePoints;
	float error_tmp;

	for(int j = 0; j < newPoints.size(); j++){
		if(status[j] > 0)
			correctPoints.push_back(newPoints[j]);

		error_tmp = error_tmp + error[j];
	}

	H12 = findHomography(Mat(templatePoints), Mat(newPoints), CV_RANSAC, 1.0);

	framePoints = newPoints;

	correctPoints.clear();

}


*/
