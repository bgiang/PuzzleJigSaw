package com.example.photojigsaw;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.photojigsaw.PuzzleActivity.ImageDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

public class LayerActivity extends Activity {
	
	private Bitmap mpic;
	Button[][] mbutton;
	ArrayList<Button> mbuttonid;
	int mheight,mwidth;
	int msize;
	boolean popped = false;
	
	private GameData gameData;
	private boolean [][] isLocked;
	private boolean [][] isComplete;
	int currX, currY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layer);
		mbuttonid=new ArrayList<Button>();
		Intent intent=getIntent();
		Random r=new Random();
		mpic=intent.getParcelableExtra("image");
		
		mheight=mpic.getHeight();
		mwidth=mpic.getWidth();
		Display display = getWindowManager().getDefaultDisplay();
		int width=display.getWidth();
		
		LinearLayout row=(LinearLayout) findViewById(R.id.grid);
		int mlayer=intent.getIntExtra("LAYER", 2);
		msize=intent.getIntExtra("difficulty", 5);
		final int dif=msize;
		
		//check for previous game data
		gameData = (GameData) intent.getSerializableExtra("game");
		if(gameData == null) {		
			//Create GameData and store on server
			gameData = new GameData(mpic, RandomStringUtils.randomAlphabetic(5), msize, true);
			UploadTask ut = new UploadTask();
			ut.execute(gameData);
		} else {
			gameData.setBitmap(mpic);
		}
		isLocked = gameData.getIsLocked();
		isComplete = gameData.getIsCompleted();
		
		//Create the button checkbox
		mbutton=new Button[msize+1][msize+1];
		for(int i=0;i<msize;i++){
			LinearLayout columns=new LinearLayout(this);
			columns.setOrientation(LinearLayout.HORIZONTAL);
			columns.setGravity(Gravity.CENTER);
			for(int j=0;j<msize;j++){
				Button temp=new Button(this);
				temp.setLayoutParams(new LinearLayout.LayoutParams((width-50)/msize,(width-50)/msize));
				temp.setId(r.nextInt());
				mbutton[i][j]=temp;
				
				if (isComplete[i][j])
				{
					mbuttonid.add(temp);
					temp.setText("DONE");
				}
				
				//Listener to go to puzzle
				temp.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Bitmap curimage;
						for(int x=0;x<dif;x++){
							for(int y=0;y<dif;y++){
								if(mbutton[x][y].getId()==v.getId() && !mbuttonid.contains(v.getId())){
									currX = x;
									currY = y;
									if(isLocked[x][y] == false && isComplete[x][y] == false) {
										//lock and save to server
										gameData.lock(x, y);
										new UploadTask().execute(gameData);
										
										//make puzzle
										curimage =  Bitmap.createBitmap(mpic, y*mwidth/dif, x*mheight/dif, mwidth/dif, mheight/dif);
										Intent puzzle=new Intent(LayerActivity.this,PuzzleActivity.class);
										//puzzle.putExtra("game", gameData);
										puzzle.putExtra("image", curimage);
										puzzle.putExtra("difficulty", dif);
										puzzle.putExtra("row", x);
										puzzle.putExtra("col", y);
										mbuttonid.add(mbutton[x][y]);
										popped = false;
										startActivityForResult(puzzle,1);
									} else {
										Toast.makeText(getApplicationContext(), "That puzzle is locked right now", Toast.LENGTH_LONG).show();
									}
								}
							}
						}
						
					}
				});
				columns.addView(temp);
				
			}
			row.addView(columns);
		}
	}

	@Override
	protected void onResume() {
		//unlock and update
		gameData.unlock(currX,  currY);
		new UploadTask().execute(gameData);
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.layer, menu);
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
		} else if (id == R.id.smsdialog) {
			SMSDialog mDialog = new SMSDialog();
			 mDialog.show(getFragmentManager(), "Sms");
		} else if (id == R.id.getId) { 
			IdDialog mDialog = new IdDialog();
			mDialog.show(getFragmentManager(), "Get Id");
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		if(requestCode==1){
			if(resultCode==RESULT_OK){
				boolean comp=data.getBooleanExtra("COMPLETE", false);
				int r = data.getIntExtra("row", -1);
				int c = data.getIntExtra("col", -1);
				if(r != -1 && c != -1) {
					gameData.complete(r, c);
					gameData.unlock(r, c);
					new UploadTask().execute(gameData);
				}
				if(comp==true){ 
					if(mbuttonid.size()>msize*msize){
						Toast.makeText(getApplicationContext(), "You finish the Puzzle!", Toast.LENGTH_LONG).show();
						finish();
					}else{
						mbuttonid.get(this.mbuttonid.size()-1).setText("DONE");
						mbuttonid.get(this.mbuttonid.size()-1).setEnabled(false);
					}
				}else {
					this.mbuttonid.remove(this.mbuttonid.size()-1);
				
				}
				
				if (this.mbuttonid.size() == msize * msize){
					 FinalDialog mDialog = new FinalDialog();
					 mDialog.show(getFragmentManager(), "Final");
					
				}
				
			
			}
		}
	}
	public class FinalDialog extends DialogFragment{
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			 builder.setMessage("You have Completed all the Section and are proceeding to the Final Puzzle")
             .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                		Intent intent=new Intent(LayerActivity.this,PuzzleActivity.class);
    					intent.putExtra("difficulty", msize);
    					intent.putExtra("image", mpic);
    					Toast.makeText(getApplicationContext(), "Final Puzzle", Toast.LENGTH_LONG).show();
    					startActivity(intent);
                 }
             });             
			 
			
			 AlertDialog dialog = builder.create();
			return  dialog;
			
		}
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
	
	public class SMSDialog extends DialogFragment {
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = (LayoutInflater)
					getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View nView = inflater.inflate(R.layout.smsdialog, null);
			final EditText phoneNum = (EditText) nView.findViewById(R.id.phone_number);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			 builder.setMessage("Enter Phone Number")
			 .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
              	 	String phoneStr = phoneNum.getText().toString();
              	 	String message = "You've been invited to play! PhotoJigsaw game id: "+ gameData.getKey();
              	 	if(message != null) {
              	 		//send SMS with game key as message
              	 		SmsManager sms = SmsManager.getDefault();
              	 		sms.sendTextMessage(phoneStr, null, message, null, null); 
              	 	}
               }
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
              	//returns
               }
           });
			 
			 builder.setView(nView);
			 AlertDialog dialog = builder.create();
			return  dialog;
		}
	}

	public class IdDialog extends DialogFragment{
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Game Id: " + gameData.getKey());
			return builder.create();
			
		}
	}
}