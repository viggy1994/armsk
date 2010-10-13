#include "Processor.h"


#include <sys/stat.h>

using namespace cv;


Processor::Processor() :
			fastd(20/*threshold*/, true/*nonmax_suppression*/),
			gfttd(200/*maxCorners*/, 0.01/*qualityLevel*/, 0.07/*blockSize*/, 3, false/*useHarrisDetector*/, 0/*if_Harris_k*/),
			stard(20/*max_size*/, 8/*response_threshold*/, 15/*line_threshold_projected*/, 8/*line_threshold_binarized*/, 5/*suppress_nonmax_size*/),
			surfd(100./*hessian_threshold*/, 1/*octaves*/, 2/*octave_layers*/)
{

}

Processor::~Processor() {
	// TODO Auto-generated destructor stub
}

void Processor::processAR(int input_idx, image_pool* pool, int detection_method) {

	FeatureDetector* fd = 0;

	switch (detection_method)
	{
		case 1 :
			fd = &fastd; // Fast Feature Detector
			break;
		case 2 :
			fd = &gfttd; // Good Features To Track Detector
			break;
		case 3 :
			fd = &stard; // Star Feature Detector
			break;
		case 4 :
			fd = &surfd; // SURF Feature Detector
			break;
		default:
			fd = 0;
	}

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


void Processor::detectAndDrawFeatures(int input_idx, image_pool* pool) {
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
