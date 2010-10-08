#include "Processor.h"


#include <sys/stat.h>

using namespace cv;

Processor::Processor() :
			fastd(20/*threshold*/, true/*nonmax_suppression*/)
{

}

Processor::~Processor() {
	// TODO Auto-generated destructor stub
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
		circle(*img, it->pt, 3, cvScalar(255, 0, 255, 0));
	}

	//pool->addImage(output_idx,outimage);

}
