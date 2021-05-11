#include <jni.h>
#include <opencv2/core.hpp>

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_oryon_heartbest_ui_finger_FingerViewModel_redAvg(JNIEnv *env, jobject thiz,
                                                                 jlong mat_addr_rgba) {
    Mat &mRgb = *(Mat *) mat_addr_rgba;
    unsigned long redSum = 0;

    int rows = mRgb.rows;
    int cols = mRgb.cols;

    unsigned long pixel = rows * cols;

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            Vec4b bgrPixel = mRgb.at<Vec4b>(i, j);

            //color.val[0] // R
            //color.val[1] // G
            //color.val[2] // B
            //color.val[3] // A

            redSum += bgrPixel.val[0];
        }
    }

    return redSum / pixel;
}