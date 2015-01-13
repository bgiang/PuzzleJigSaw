package com.example.photojigsaw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.commons.lang3.RandomStringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PuzzleActivity extends Activity 
{
	private Bitmap mpic;
	
	private ActualGame theGame;
	private Piece [][] allPieces;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle);
		
		Intent intent=getIntent();
		
		mpic=intent.getParcelableExtra("image");
		
		int difficulty = intent.getExtras().getInt("difficulty");

		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.frame);
		Bitmap scaledImage = Bitmap.createScaledBitmap(mpic, 1000, 1000, false);
        
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfView = size.x;
        int heightOfView = size.y;
        
        createPuzzle (scaledImage, scaledImage.getWidth(), difficulty);
        
        int r = intent.getIntExtra("row", -1);
        int c = intent.getIntExtra("col", -1);
        
        theGame = new ActualGame(this, heightOfView, widthOfView, r, c);
        theGame.setPuzzle(allPieces);
        relativeLayout.addView(theGame);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.puzzle, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}else if(id==R.id.imagedialog){
			 ImageDialog mDialog = new ImageDialog();
			 mDialog.show(getFragmentManager(), "Image");
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	public class ImageDialog extends DialogFragment{
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = (LayoutInflater)
					getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View nView = inflater.inflate(R.layout.imagedialog, null);
			ImageView mDiag=(ImageView) nView.findViewById(R.id.diagimage);
			mDiag.setImageBitmap(mpic);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			 builder.setMessage("Image")
           
             .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                	
                	
                 }
             });
			 
			 builder.setView(nView);
			 AlertDialog dialog = builder.create();
			return  dialog;
			
		}
	}
    
    public void createPuzzle(Bitmap puzzleResult, int puzzleDim, int total) 
    {
    	int pieceDim = puzzleDim / total;

        allPieces = new Piece [total][total];

        for (int x = 0; x < total; x++) 
        {
            for (int y = 0; y < total; y++) 
            {
            	//creating a new piece that will have in it the bitmap (that we get from the original)
            	//and also making sure that this piece is not locked in place)
            	Piece toAdd;
            	Bitmap currentImage =  Bitmap.createBitmap(puzzleResult, x*pieceDim, y*pieceDim, pieceDim, pieceDim);
            	toAdd = new Piece (currentImage);
            	toAdd.disabled = false;
            	allPieces[x][y] = toAdd;
            }
        }
        
        //Make a GameData object and store on server
        Log.d("PuzzleActivity", "Saving Puzzle");
        GameData gameData = new GameData(puzzleResult, RandomStringUtils.randomAlphabetic(5), total, false);
        UploadTask ut = new UploadTask();
        ut.execute(gameData);

    }
    
    private class UploadTask extends AsyncTask<GameData, Void, Void> {
    	
    	private final static String MY_ACCESS_KEY_ID = "AKIAII53JO2V4237CDGQ";
    	private final static String MY_SECRET_KEY = "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";
    	
    	@Override
    	protected Void doInBackground(GameData... params) {
    		Log.d("Upload Task", "Uploading...");
    		try {
    			BasicAWSCredentials awsCreds = new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY);
    			AmazonS3 s3Client = new AmazonS3Client(awsCreds);
    			
    			Bitmap b = params[0].getBitmap();
				File data = new File(getApplicationContext().getCacheDir(), "data");
				File image = new File(getApplicationContext().getCacheDir(), "/image");
    			data.createNewFile();
    			image.createNewFile();
    			
    			Log.d("Upload Task", "trying to upload to folder " + params[0].getKey());
    		
    			ByteArrayOutputStream bos = new ByteArrayOutputStream();
    			b.compress(CompressFormat.PNG, 0, bos);
    			byte[] bitmapdata = bos.toByteArray();
    	
    			FileOutputStream fos = new FileOutputStream(image);
    			fos.write(bitmapdata);
    			fos.flush();
    			fos.close();
    			
    			FileOutputStream dos = new FileOutputStream(data);
    			ObjectOutputStream oos = new ObjectOutputStream(dos);
    			oos.writeObject(params[0]);
    			oos.flush();
    			oos.close();
    			dos.flush();
    			dos.close();
    			
    			s3Client.putObject(new PutObjectRequest("photojigsaw", params[0].getKey() + "/image", image));
    			s3Client.putObject(new PutObjectRequest("photojigsaw", params[0].getKey() + "/data", data));
    			
    			} catch (IOException e) { 
    			e.printStackTrace();    
    		}
    		return null;
    	}

    }
    
}