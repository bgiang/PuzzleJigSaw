package com.example.photojigsaw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
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
        
        theGame = new ActualGame(this, heightOfView, widthOfView);
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

    }
    
}