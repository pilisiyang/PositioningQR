package com.example.demo;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DemoAction {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {

        //读取原图像
        Mat image = Imgcodecs.imread("D:\\test.png");
        //建立灰度图像存储空间
        Mat gray = new Mat(image.rows(), image.cols(), CvType.CV_8UC1);
        //彩色图像灰度化
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);
        //高斯模糊
        Mat gauss = gray.clone();
        Imgproc.GaussianBlur(gray, gauss, new Size(new Point(5, 5)), 0);
        // 函数检测边缘
        Mat canny = gauss.clone();
        Imgproc.Canny(gauss, canny, 100, 200);

        //找到轮廓
        Mat hierarchy = canny.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Integer> ints = new ArrayList<>();
        List<MatOfPoint> points = new ArrayList<MatOfPoint>();
        //从轮廓的拓扑结构信息中得到有5层以上嵌套的轮廓
        for (int i = 0; i < contours.size(); i++) {
            int k = i;
            int c = 0;
            while (hierarchy.get(0, k)[2] != -1) {
                k = (int) hierarchy.get(0, k)[2];
                c = c + 1;
                if (c >= 5) {
                    ints.add(i);
                    points.add(contours.get(i));
                }
            }
        }
        System.out.println("找到" + ints.size() + "个标志轮廓!");

        Point[] point = convertPoints(points);

        //轮廓转换成最小矩形包围盒
        RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(point));
        //截取出二维码
        Rect qrRect = rotatedRect.boundingRect();
        Mat qrCodeImg = new Mat(image, qrRect);
        //保存图像
        Imgcodecs.imwrite("D:\\qrCodeImg.jpg", qrCodeImg);

        //矩形左上顶点的坐标
        Point tl = qrRect.tl();
        //矩形右下顶点的坐标
        Point br = qrRect.br();
        double width = (br.x - tl.x);
        double height = (br.y - tl.y);

        Rect rect = new Rect((int) tl.x + (int) width / 3, (int) br.y + (int) height / 3, (int) width / 3, (int) height);
        //截取出检验区域
        Mat testImg = new Mat(image, rect);
        //保存图像
        Imgcodecs.imwrite("D:\\testImg.jpg", testImg);

    }

    /**
     * 从MatOfPoint中提取point
     *
     * @param points
     * @return
     */
    private static Point[] convertPoints(List<MatOfPoint> points) {
        Point[] points1 = points.get(0).toArray();
        Point[] points2 = points.get(1).toArray();
        Point[] points3 = points.get(2).toArray();

        Point[] point = new Point[points1.length + points2.length + points3.length];
        System.arraycopy(points1, 0, point, 0, points1.length);
        System.arraycopy(points2, 0, point, points1.length, points2.length);
        System.arraycopy(points3, 0, point, points1.length + points2.length, points3.length);
        return point;
    }
}
