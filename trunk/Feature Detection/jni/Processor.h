#ifndef PROCESSOR_H_
#define PROCESSOR_H_

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>



#include <vector>

#include "image_pool.h"

class Processor {

	cv::FastFeatureDetector fastd;
	cv::GoodFeaturesToTrackDetector gfttd;
	cv::StarFeatureDetector stard;
	cv::SurfFeatureDetector surfd;
	std::vector<cv::KeyPoint> keypoints;

	//image_pool pool;
public:
	
	Processor();
	virtual ~Processor();
	
	void processAR(int idx, image_pool* pool, int detection_method);
	void detectAndDrawFeatures(int idx, image_pool* pool);
	
};

#endif /* PROCESSOR_H_ */
