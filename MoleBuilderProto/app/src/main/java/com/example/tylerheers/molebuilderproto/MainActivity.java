package com.example.tylerheers.molebuilderproto;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity
{
    private ImageView downloadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadImage = (ImageView)findViewById(R.id.downloadImage);

        new ImageDownloader(this).execute("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/adenine/PNG");
    }

    public void setDownloadImage(Bitmap bitmap){
        downloadImage.setImageBitmap(bitmap);
    }
}
