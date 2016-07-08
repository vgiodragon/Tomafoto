package com.example.giovanny.tomafoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String  TAG                 = "OCVSample::Activity";


    //private CameraBridgeViewBase mOpenCvCameraView;
    private Tutorial3View mOpenCvCameraView;
    int foto;
    boolean foto2;

    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final Scalar FACE_RECT_COLOR2     = new Scalar(255, 0, 0, 255);
    private static final Scalar FACE_RECT_COLOR3     = new Scalar(0, 0, 255, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;


    public int mwidth;
    public int mheight;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
        foto=10;
        foto2=false;
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        //mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //mwidth =displayMetrics.widthPixels;
        //mheight=displayMetrics.heightPixels;

        Log.d("corta","w:"+mwidth+"__"+mheight);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {

            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            Toast.makeText(this,"JAVA_"+mItemType+"_l_"+tmpDetectorType,Toast.LENGTH_SHORT).show();
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mwidth=width;
        mheight=height;
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public synchronized int getfoto(){
        return foto;
    }
    public synchronized void restfoto(){
     foto--;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        String posiciones="";
/*
        Point p1= new Point();
        Point p2= new Point();
        mwidth =mOpenCvCameraView.getWidth();
        mheight=mOpenCvCameraView.getHeight();

        p1.set(new double[]{0,0});
        p2.set(new double[]{1280,720});
        Imgproc.rectangle(mRgba, p1, p2, FACE_RECT_COLOR2, 2);
*/
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 2);
            posiciones+=facesArray[i].tl()+"_"+facesArray[i].br()+"__";
        }

        if(facesArray.length>0 && foto>0){

            Log.d("corta","w:"+mwidth+"__"+mheight);
            String npath = TomaFoto();
            Log.d("corta","Cuadrados_"+posiciones);
            for (int i = 0; i < facesArray.length; i++) {
                new HiloGuardaCortaManda(i, npath, facesArray[i].x, facesArray[i].y,
                        facesArray[i].width, facesArray[i].height).execute();
                restfoto();
            }

            foto2=true;
            Log.d("corta","_"+foto);
        }

        return mRgba;
    }
    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

    public String TomaFoto() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/Final/"+currentDateandTime+ ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        return fileName;
    }

    public void CortaryGuardar(int i,String path, int xi, int yi, int wi, int hi){

                File imgFile = new File(path);
                while (!imgFile.exists()) {
                    imgFile = new File(path);
                }
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                int altura = bitmap.getHeight();
                int ancho = bitmap.getWidth();

                float proporW=ancho / mwidth;
                float proporH=altura / mheight;
                int x = (int) (xi * proporW);
                int y = (int) (yi * proporH);

                int w = (int) (wi * proporW);
                int h = (int) (hi * proporH);
                Bitmap cortado = Bitmap.createBitmap(bitmap, x, y, w, h);
                FileOutputStream fos = null;
                try {

                    String []partes = path.split(".jp");
                    String fileName = partes[0] +i+ ".jpg";
                    fos = new FileOutputStream(fileName);

                    cortado.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


    }

    public class HiloGuardaCortaManda extends AsyncTask<String, Void, Void> {
        int xi,yi,wi,hi;
        String path;
        int i;

        public HiloGuardaCortaManda(int i,String path,int xi, int yi, int wi, int hi) {
            this.xi = xi;
            this.yi = yi;
            this.wi = wi;
            this.hi = hi;
            this.path=path;
            this.i=i;
        }

        @Override
        protected Void doInBackground(String... args) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CortaryGuardar(i,path,xi,yi,wi,hi);

            return null;
        }

    }

}