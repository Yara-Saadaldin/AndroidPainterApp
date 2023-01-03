package com.example.imd4008a__assignment4_onyinyeagetu_yarasaadaldin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    private ImageButton btnUndo, btnRedo,  btnLoad, btnSave,  btnClear,  btnCircle, btnLine,  btnRectangle;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private void init() {
        paintView = findViewById(R.id.paintView);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnLoad = findViewById(R.id.btnLoad);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnCircle = findViewById(R.id.btnCircle);
        btnLine = findViewById(R.id.btnLine);
        btnRectangle = findViewById(R.id.btnRectangle);
    }

    private void defaultValues() {
        paintView.setColor(getResources().getColor(R.color.black));
        paintView.setStrokeWidth(8);
    }

    private void btnClicks() {
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.undo();
            }
        });

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.redo();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.clear_message));

                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paintView.clearCanvas();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = paintView.save();

                AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                saveDialog.setTitle("save?");
                saveDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String savedImg = MediaStore.Images.Media.insertImage(
                                getContentResolver(), paintView.getDrawingCache(), UUID.randomUUID().toString() + ".png", "sketch"
                        );
                        if (savedImg != null) {
                            Toast savedToast = Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_LONG);
                            savedToast.show();
                        } else {
                            Toast unsavedToast = Toast.makeText(getApplicationContext(), " not saved", Toast.LENGTH_LONG);
                            unsavedToast.show();
                        }
                        paintView.destroyDrawingCache();
                    }
                });
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setLineMode(false);
                paintView.setCircleMode(true);
                paintView.setRectangleMode(false);
            }
        });

        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setLineMode(true);
                paintView.setRectangleMode(false);
            }
        });

        btnRectangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setLineMode(false);
                paintView.setCircleMode(false);
                paintView.setRectangleMode(true);
            }
        });
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        defaultValues();
        btnClicks();
    }
}