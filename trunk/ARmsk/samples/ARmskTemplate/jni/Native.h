#ifndef NATIVE_H_
#define NATIVE_H_

#include "ARmsk.h"

#include <sys/stat.h>
#include <algorithm>


using namespace ARmsk;


class Native {

	cv::SurfFeatureDetector surfd;
	cv::SurfDescriptorExtractor surfe;

	

public:

	Native();
	virtual ~Native();
	
	void processAR(int input_idx, image_pool* pool);
	
	void match(IMAGEDATA &trainData, IMAGEDATA &queryData, vector<int> &matches);
	
	void setMarker(int input_idx, image_pool* pool);
    void setMarker(const char * filePath);
	
	void clearAll();
	
	float getMatrix(int i);
	float getScale();
	int getMatchFound();
	void setDrawMatchResult();
	
};

#endif /* ARMSK_H_ */


//void saveMarker(int input_idx, image_pool* pool, const char * filePath );
//void findFeatures(const char * filePath, const char * dstPath  );
//int getNumberFeatures();
