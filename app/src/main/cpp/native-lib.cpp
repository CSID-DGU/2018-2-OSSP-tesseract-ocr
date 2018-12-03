#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

extern "C" {
JNIEXPORT void JNICALL
Java_com_bluebead38_opencvtesseractocr_CameraView_imageprocessing(
        JNIEnv * env,
        jobject,
        jlong
        addrInputImage
) {

    Mat &img_input = *(Mat *) addrInputImage;
    Mat img_output;
    img_input.copyTo(img_output);

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    cvtColor( img_input, img_input, CV_BGR2RGB);
    cvtColor( img_input, img_output, CV_RGB2GRAY);
    blur( img_output, img_output, Size(3, 3));
    Canny( img_output, img_output, 100, 200);
    findContours(img_output, contours, hierarchy, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
    sort(contours.begin(), contours.end(), [](const vector<Point>& c1, const vector<Point>& c2)
    {
        return contourArea(c1, false) < contourArea(c2, false);
    });

    vector<vector<Point>> top5_contours;
    vector<vector<Point>> contours_poly(4);
    vector<Point> screen;

    for (int i = 1; i < 5; i++){
        if (contours.size() > i) {
            top5_contours.push_back(contours[contours.size() - i]);
        }
    }
    for (int i = 0; i < top5_contours.size(); i++) {
        double peri = arcLength(Mat(top5_contours[i]), true);
        approxPolyDP(Mat(top5_contours[i]), contours_poly[i], peri * 0.02, true);
        if(contours_poly[i].size() == 4){
            screen = contours_poly[i];
            break;
        }
    }
    Point TopLeft = screen[0];
    Point TopRight = screen[1];
    Point BottomRight = screen[2];
    Point BottomLeft = screen[3];
    double max = INT_MIN;
    double min = INT_MAX;

    for(int i = 0; i < 4; i++){
        double xysum = screen[i].x + screen[i].y;
        if(max < xysum){
            max = xysum;
            BottomRight = screen[i];
        }
        if(min > xysum){
            min = xysum;
            TopLeft = screen[i];
        }
    }

    max = INT_MIN;
    min = INT_MAX;

    for(int i = 0; i < 4; i++){
        double xysum = screen[i].y - screen[i].x;
        if(max < xysum){
            max = xysum;
            BottomLeft = screen[i];
        }
        if(min > xysum){
            min = xysum;
            TopRight = screen[i];

        }
    }

    double w1 = sqrt( pow(BottomRight.x - BottomLeft.x, 2)
                      + pow(BottomRight.x - BottomLeft.x, 2) );
    double w2 = sqrt( pow(TopRight.x - TopLeft.x, 2)
                      + pow(TopRight.x - TopLeft.x, 2) );

    double h1 = sqrt( pow(TopRight.y - BottomRight.y, 2)
                      + pow(TopRight.y - BottomRight.y, 2) );
    double h2 = sqrt( pow(TopLeft.y - BottomLeft.y, 2)
                      + pow(TopLeft.y - BottomLeft.y, 2) );

    double maxWidth = (w1 < w2) ? w1 : w2;
    double maxHeight = (h1 < h2) ? h1 : h2;

    Point2f src[4], dst[4];
    src[0] = Point2f(TopLeft.x, TopLeft.y);
    src[1] = Point2f(TopRight.x, TopRight.y);
    src[2] = Point2f(BottomRight.x, BottomRight.y);
    src[3] = Point2f(BottomLeft.x, BottomLeft.y);

    dst[0] = Point2f(0, 0);
    dst[1] = Point2f(maxWidth-1, 0);
    dst[2] = Point2f(maxWidth-1, maxHeight-1);
    dst[3] = Point2f(0, maxHeight-1);

    Mat trans = getPerspectiveTransform(src, dst);
    warpPerspective(img_input, img_input, trans, Size(maxWidth, maxHeight));
    cvtColor( img_input, img_input, CV_BGR2GRAY);
    adaptiveThreshold(img_input, img_input, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 21, 10);
}
}
