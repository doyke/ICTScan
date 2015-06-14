package org.chocolatemilk.ictscan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;


public class start extends ActionBarActivity {

    ImageView viewImage;
    TextView textInfo, textTest;
    Button b;
    Button btnConfirm;

    Bitmap thumbnail;

    DragPointView [] points;
    PointF [] coordinates;

    int index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (!OpenCVLoader.initDebug())
        {
            //Handle initialization error
        }

        b = (Button) findViewById(R.id.btnSelectPhoto);//Button aus dem xml der Activity
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        viewImage = (ImageView) findViewById(R.id.viewImage);//ImageView -----"-----
        textInfo = (TextView) findViewById(R.id.textInfo);//TextView-------"-----
        textTest = (TextView) findViewById(R.id.textTest);//TextView-------"-----

        points = new DragPointView[4];
        points[0] = (DragPointView) findViewById(R.id.dragPointTL);
        points[1] = (DragPointView) findViewById(R.id.dragPointTR);
        points[2] = (DragPointView) findViewById(R.id.dragPointBR);
        points[3] = (DragPointView) findViewById(R.id.dragPointBL);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        //Point marking the corners
        if (null != points[0]) {

            points[0].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                    Toast.makeText(getApplicationContext(), "TL: Point is (" + point.x+ ", " + point.y + ")",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        if (null != points[1]) {

            points[1].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                    Toast.makeText(getApplicationContext(), "TR: Point is (" + point.x+ ", " + point.y + ")",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        if (null != points[2]) {

            points[2].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                    Toast.makeText(getApplicationContext(), "BR: Point is (" + point.x+ ", " + point.y + ")",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        if (null != points[3]) {

            points[3].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                    Toast.makeText(getApplicationContext(), "BL: Point is (" + point.x+ ", " + point.y + ")",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start, menu);
        //inflater.inflate(R.menu.action_restart, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // Handle presses on the action bar items
        switch (id) {
            case R.id.action_restart:
                openRestart();
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            case R.id.action_resetPoints:
                openResetPoints();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(start.this); //Dialog erstellen
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) //Photo machen
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "ICT.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) //Photo waehlen
                {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //request code entspricht was vorher ausgewählt wurde -> neues Foto oder aus Gallerie
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) { //took new photo
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("ICT.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    thumbnail = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    viewImage.setImageBitmap(thumbnail);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    //f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Reset the points and corresponding layouts
                resetForDrawing();

            }
            else if (requestCode == 2) { //chose photo from gallery

                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.w("path of image from gallery......******************.........", picturePath+"");
                viewImage.setImageBitmap(thumbnail);

                //Reset the points and corresponding layouts
                resetForDrawing();

            }
        }
    }

    private void confirm() {
        if (points[index].getX() <= 0 && points[index].getX() <= 0)
        {
            //Punkt noch nicht gesetzt
            Toast.makeText(getApplicationContext(), "Position your point inside the picture",
                    Toast.LENGTH_LONG).show();
            return;
        }
        boolean control = false;
        if (index==0)
        {
            control = true; //Erster Punkt darf beliebig gesetzt werden
        }
        else if (index == 1)
        {
            if (points[1].getCurX() > points[0].getCurX())
            {
                //nur dann erlauben: zweiter Punkt ist rechts von erstem Punkt
                control = true;
            }
        }
        else if (index == 2)
        {
            if (points[2].getCurY() > points[1].getCurY())
            {
                //nur dann erlauben: dritter Punkt ist unterhalb des zweiten Punkts
                control = true;
            }
        }
        else if (index==3)
        {
            if ((points[3].getCurX() < points[2].getCurX()) && (points[3].getCurY() > points[0].getCurY()))
            {
                //nur dann erlauben: letzter Punkt ist unterhalb von erstem und links vom dritten Punkt
                control = true;
            }
        }

        //Wenn Position des Punktes korrekt gewählt wurde
        if (!control)
        {
            //Anzeige, dass Punkt falsch gesetzt wurde!
            Toast.makeText(getApplicationContext(), "Check the position of your last point",
                    Toast.LENGTH_LONG).show();
        }
        else if (index!=3)
        {
            points[index].readyForTouch = false;
            points[index].fix_coordinates();
            index++;
            points[index].readyForTouch = true;
            points[index].setVisibility(View.VISIBLE);
            textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index]);
        }

        else
        {
            points[index].readyForTouch = false;
            points[index].fix_coordinates();
            //Last Point was checked, so don't increment index
            btnConfirm.setVisibility(View.INVISIBLE); //Hide OK-Button

            for (int i = 0; i<4; i++)
            {
                points[i].setVisibility(View.GONE);
            }

            imageManipulation();
        }
    }

    public void resetForDrawing()
    {
        //Reset the points and corresponding layouts
        b.setVisibility(View.GONE);
        for (int i = 1; i<4; i++)
        {
            points[i].setVisibility(View.GONE);
            points[i].reset();
            points[i].viewImage = viewImage;
        }
        points[0].reset();
        points[0].viewImage = viewImage;
        points[0].readyForTouch=true;
        points[0].setVisibility(View.VISIBLE);
        textInfo.setText(getResources().getStringArray(R.array.textView_corners)[0]);
        btnConfirm.setVisibility(View.VISIBLE);
        index = 0;
    }

    public void perspectiveTransformation()
    {
        Bitmap image = ((BitmapDrawable)viewImage.getDrawable()).getBitmap();

        int resultWidth = 500;
        int resultHeight = 500;

        Mat inputMat = new Mat(image.getHeight(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, inputMat);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        org.opencv.core.Point ocvPIn1 = new org.opencv.core.Point(points[0].getFinalX(), points[0].getFinalY());
        org.opencv.core.Point ocvPIn2 = new org.opencv.core.Point(points[1].getFinalX(), points[1].getFinalY());
        org.opencv.core.Point ocvPIn3 = new org.opencv.core.Point(points[2].getFinalX(), points[2].getFinalY());
        org.opencv.core.Point ocvPIn4 = new org.opencv.core.Point(points[3].getFinalX(), points[3].getFinalY());

        List<org.opencv.core.Point> source = new ArrayList<org.opencv.core.Point>();
        source.add(ocvPIn1);
        source.add(ocvPIn4);
        source.add(ocvPIn3);
        source.add(ocvPIn2);


        Mat startM = Converters.vector_Point2f_to_Mat(source);

        org.opencv.core.Point ocvPOut1 = new org.opencv.core.Point(0, 0);
        org.opencv.core.Point ocvPOut2 = new org.opencv.core.Point(0, resultHeight);
        org.opencv.core.Point ocvPOut3 = new org.opencv.core.Point(resultWidth, resultHeight);
        org.opencv.core.Point ocvPOut4 = new org.opencv.core.Point(resultWidth, 0);
        List<org.opencv.core.Point> dest = new ArrayList<org.opencv.core.Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        viewImage.setImageBitmap(output);
    }


    //Menu option restart
    private void openRestart(){
        //reset the drag points
        for (int i = 0; i<4; i++ )
        {
            points[i].reset();
            points[i].setVisibility(View.GONE);
        }
        //"delete" the shown image
        viewImage.setImageResource(R.drawable.ic_action_camera);
        //show -select image- objects
        b.setVisibility(View.VISIBLE);
        textInfo.setText(R.string.textView_start);
        //hide confirm-button
        btnConfirm.setVisibility(View.INVISIBLE);
    }

    private void openResetPoints(){

        final CharSequence[] options = { "Reposition last point", "Reposition all points" };

        AlertDialog.Builder builder = new AlertDialog.Builder(start.this); //Dialog erstellen
        builder.setTitle("Reposition your points!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Reposition last point")) //Only reposition the last point
                {
                    points[index].readyForTouch = false;
                    points[index].setVisibility(View.GONE);
                    index--;
                    points[index].reset();
                    textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index]);
                    points[index].readyForTouch = true;
                    points[index].setVisibility(View.VISIBLE);
                } else if (options[item].equals("Reposition all points")) //reset all points
                {
                    resetForDrawing();
                }
            }
        });
        builder.show();

    }

    private void edgeDetection(){
        Bitmap input = ((BitmapDrawable)viewImage.getDrawable()).getBitmap();
        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        //grayscale
        Mat image = new Mat (input.getWidth(), input.getHeight(), CvType.CV_32F);
        Mat gray = new Mat (input.getWidth(), input.getHeight(), CvType.CV_32F);
        Mat sobel = new Mat (input.getWidth(), input.getHeight(), CvType.CV_32F);
        Utils.bitmapToMat(input, image);
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);
        //Utils.matToBitmap(gray, output);

        Imgproc.Sobel(gray, sobel, -1, 1, 0); //sobel

        Utils.matToBitmap(sobel, output);
        viewImage.setImageBitmap(output);


        //Histogram
        /*ArrayList<Mat> listMat = new ArrayList<Mat>();
        listMat.add(sobel);
        MatOfInt one = new MatOfInt(0);
        Mat hist= new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f,256f);

        Imgproc.calcHist(listMat, one, new Mat(), hist, histSize, ranges);

        Scalar cdf = Core.sumElems(hist);
        Core.MinMaxLocResult result = Core.minMaxLoc( hist);
        double minHist = result.minVal;
        double maxHist = result.maxVal;
        /*Scalar cdfNormalized = cdf*minHist/cdf;*/
        /*org.opencv.core.Point histMaxIndex = result.maxLoc; // hellster Punkt
        org.opencv.core.Point histMinIndex = result.minLoc;

        /*img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY);
        hist, bins = np.histogram(img_gray.flatten(), 256, [0, 256]);
        cdf = hist.cumsum()
        cdf_normalized = cdf * hist.max()/ cdf.max()
        hist_max_index = np.argmax(hist[25:200:1])
        hist_min_index = hist_max_index + np.argmin(hist[hist_max_index:hist_max_index + 50]);
        img_gray[np.where(img_gray > hist_min_index)] = 255
        img_gray[np.where(img_gray <= hist_min_index)] = 0
        img_sobel = cv2.Sobel(img_gray, cv2.CV_32F, 1, 0);
        img_edge = np.zeros(img_gray.shape, dtype = np.uint8)
        img_edge[np.where(img_sobel > 1000)] = 255
        img_edge[np.where(img_sobel < -1000)] = 255*/
    }

    private boolean imageManipulation()
    {
        perspectiveTransformation();
        edgeDetection();
        return true;
    }
 }
