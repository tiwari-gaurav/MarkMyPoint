package com.markmypoint.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.markmypoint.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/*This class will select an image from a gallery.
* User can draw multiple squares on a image*/

public class MainActivity extends AppCompatActivity {

    Button btnLoadImage, btnCoordinate;
    TextView textSource, showCoordinates;
    ImageView selectedImage, imageDrawingPane;

    final int RQS_CODE = 1;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;
    Bitmap bitmapDrawingPane;
    Canvas canvasDrawingPane;
    DrawView startPt;
    List<Point> points;
    int projectedX;
    int projectedY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        points = new ArrayList<Point>();
        btnCoordinate = (Button) findViewById(R.id.coordinates);
        btnLoadImage = (Button) findViewById(R.id.loadimage);
        textSource = (TextView) findViewById(R.id.sourceuri);
        showCoordinates = (TextView) findViewById(R.id.showCoordinates);
        selectedImage = (ImageView) findViewById(R.id.result);
        imageDrawingPane = (ImageView) findViewById(R.id.drawingpane);

        btnLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_CODE);
                if (points.size() > 0) {
                    points.removeAll(points);
                }
            }
        });

        btnCoordinate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to get the coordinates of a square
                getCoordinates(points);
            }
        });

        selectedImage.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        textSource.setText("ACTION_DOWN- " + x + " : " + y);
                        startPt = projectXY((ImageView) v, bitmapMaster, x, y);
                        points.add(new Point(startPt.x, startPt.y));

                        break;
                    case MotionEvent.ACTION_MOVE:
                        textSource.setText("ACTION_MOVE- " + x + " : " + y);
                        drawOnRectProjectedBitMap((ImageView) v, bitmapMaster, x, y);

                        break;
                    case MotionEvent.ACTION_UP:
                        textSource.setText("ACTION_UP- " + x + " : " + y);
                        drawOnRectProjectedBitMap((ImageView) v, bitmapMaster, x, y);
                        points.add(new Point(x, y));
                        points.add(new Point(v.getWidth(), v.getHeight()));
                        points.add(new Point(bitmapMaster.getWidth(), bitmapMaster.getHeight()));
                        finalizeDrawing();
                        break;
                }

                return true;
            }
        });


    }

    private void getCoordinates(List<Point> points) {
        if (points.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < points.size(); i++) {
                Log.i("coordinates", points.get(i).toString());
                if ((i + 1) % 4 != 0) {
                    builder.append(points.get(i) + ",");

                }else{
                    builder.append( points.get(i)+ "}]" + "\n"+",{ coordinates:[");
                }

            }
            textSource.setText("[{coordinates:[" + builder.toString() + "]}]");
        }
    }

    class DrawView {
        int x;
        int y;

        DrawView(int tx, int ty) {
            x = tx;
            y = ty;
        }
    }

    private DrawView projectXY(ImageView iv, Bitmap bm, int x, int y) {
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return null;
        } else {
            int projectedX = (int) ((double) x * ((double) bm.getWidth() / (double) iv.getWidth()));
            int projectedY = (int) ((double) y * ((double) bm.getHeight() / (double) iv.getHeight()));

            return new DrawView(projectedX, projectedY);
        }
    }

    private void drawOnRectProjectedBitMap(ImageView iv, Bitmap bm, int x, int y) {
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        } else {

            projectedX = (int) ((double) x * ((double) bm.getWidth() / (double) iv.getWidth()));
            projectedY = (int) ((double) y * ((double) bm.getHeight() / (double) iv.getHeight()));

            //clear canvasDrawingPane
            canvasDrawingPane.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(3);
            canvasDrawingPane.drawRect(startPt.x, startPt.y, projectedX, projectedY, paint);
            imageDrawingPane.invalidate();


            textSource.setText(x + ":" + y + "\n" +
                    projectedX + " : " + projectedY
            );
        }


    }

    private void finalizeDrawing() {
        canvasMaster.drawBitmap(bitmapDrawingPane, 0, 0, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQS_CODE:
                    source = data.getData();
                    textSource.setText(source.toString());

                    try {

                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(source));

                        Bitmap.Config config;
                        if (tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }


                        bitmapMaster = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);

                        canvasMaster = new Canvas(bitmapMaster);
                        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        selectedImage.setImageBitmap(bitmapMaster);

                        //Create bitmap of same size for drawing
                        bitmapDrawingPane = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);
                        canvasDrawingPane = new Canvas(bitmapDrawingPane);
                        imageDrawingPane.setImageBitmap(bitmapDrawingPane);


                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                    }

                    break;
            }
        }
    }
}
