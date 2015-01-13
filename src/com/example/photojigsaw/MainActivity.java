package com.example.photojigsaw;

import java.io.FileNotFoundException;
import java.io.IOException;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;

public class MainActivity extends Activity {
	static final int REQUEST_IMAGE_CAPTURE = 1;
	ImageView mimage;
	private DialogFragment mDialog;
	Button mMake;
	private RadioButton mEasy,mMed,mHard,mCustom;
	private int difficulty,mlayer;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button mCam=(Button) findViewById(R.id.camera);
		Button mPic=(Button) findViewById(R.id.gallery);
		
		mMake =(Button) findViewById(R.id.make);
		mimage=(ImageView) findViewById(R.id.picture);
		mMake.setEnabled(false);
		mCam.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			    }
			}
		});
		mPic.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				
				startActivityForResult(photoPickerIntent, 2);
			}
			
		});
		
		mMake.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Bitmap mBit;
				mBit=((BitmapDrawable)mimage.getDrawable()).getBitmap();
				Intent intent= new Intent();
				intent.putExtra("image", mBit);
				
				if (mEasy.isChecked()){
					difficulty = 5;
					mlayer=1;
				}else if (mMed.isChecked()){
					difficulty = 8;
					mlayer=1;
				}else if (mHard.isChecked()){
					difficulty = 11;
					mlayer=1;
				}
				
				intent.putExtra("difficulty", difficulty);
				
				//need to get all of the information about puzzle to make. 
				//including number of levels, number of tiles, etc..
				
				if(mlayer==1){
					intent.setClass(MainActivity.this,PuzzleActivity.class);
					startActivity(intent);
				}else{
					intent.setClass(MainActivity.this,LayerActivity.class);
					startActivity(intent);
				}
			}
		});
		
		radioSetup();
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	        Bundle extras = data.getExtras();
	        Bitmap imageBitmap = (Bitmap) extras.get("data");
	        mimage.setImageBitmap(imageBitmap);
	    }else if(requestCode == 2 && resultCode == RESULT_OK){
	    	  Uri chosenImageUri = data.getData();
	    	  String[] filePathColumn = { MediaStore.Images.Media.DATA };
	    	  
	          Cursor cursor = getContentResolver().query(chosenImageUri,
	                  filePathColumn, null, null, null);
	          cursor.moveToFirst();
	  
	          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	          String picturePath = cursor.getString(columnIndex);
	          cursor.close();
	          mimage.setImageBitmap(getScaledBitmap(picturePath, 200, 200));
	    }
	    mMake.setEnabled(true);
	}
	
	public class CustomDialog extends DialogFragment{
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = (LayoutInflater)
					getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View nView = inflater.inflate(R.layout.customdialog, null);
			
			final NumberPicker size=(NumberPicker) nView.findViewById(R.id.puzzlesize);
			final NumberPicker layer=(NumberPicker) nView.findViewById(R.id.layer);
			
			size.setMaxValue(13);
			size.setMinValue(2);
			layer.setMinValue(1);
			layer.setMaxValue(2);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			 builder.setMessage("Choose a custom size and layer for the puzzle")
             .setNegativeButton("Set", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                	 	mEasy.setChecked(false);
                	 	mMed.setChecked(false);
     	            	mHard.setChecked(false);
     	            	mCustom.setChecked(true);
     	            
     	            	difficulty = size.getValue();
     	            	mlayer=layer.getValue();
                 }
             })
             .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                	 if(mHard.isChecked()||mEasy.isChecked()||mMed.isChecked())
                		 mCustom.setChecked(false); 
                	
                 }
             });
			 
			 builder.setView(nView);
			 AlertDialog dialog = builder.create();
			return  dialog;
			
		}
	}
	
	private void radioSetup(){
		mEasy=(RadioButton) findViewById(R.id.easy);
		mMed=(RadioButton) findViewById(R.id.medium);
		mHard=(RadioButton) findViewById(R.id.hard);
		mCustom=(RadioButton) findViewById(R.id.custom);
		
		mEasy.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            mEasy.setChecked(true);
	            mMed.setChecked(false);
	            mHard.setChecked(false);
	            mCustom.setChecked(false);
	        }
	    });

	    mMed.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            mEasy.setChecked(false);
	            mMed.setChecked(true);
	            mHard.setChecked(false);
	            mCustom.setChecked(false);
	        }
	    });

	    mHard.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            mEasy.setChecked(false);
	            mMed.setChecked(false);
	            mHard.setChecked(true);
	            mCustom.setChecked(false);
	        }
	    });
	    
	    
	    //TODO: I need information about what the user puts in here
	    mCustom.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            mDialog=new CustomDialog();
	            mDialog.show(getFragmentManager(), "Custom");
	        }
	    });
	}
	
	private Bitmap getScaledBitmap(String picturePath, int width, int height) {
	    BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
	    sizeOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(picturePath, sizeOptions);

	    int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

	    sizeOptions.inJustDecodeBounds = false;
	    sizeOptions.inSampleSize = inSampleSize;

	    return BitmapFactory.decodeFile(picturePath, sizeOptions);
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and
	        // width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will
	        // guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
}