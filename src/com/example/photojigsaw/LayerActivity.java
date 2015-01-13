package com.example.photojigsaw;

import java.util.ArrayList;
import java.util.Random;

import com.example.photojigsaw.PuzzleActivity.ImageDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
				//Listener to go to puzzle
				temp.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						Bitmap curimage;
						for(int x=0;x<dif;x++){
							for(int y=0;y<dif;y++){
								if(mbutton[x][y].getId()==v.getId() && !mbuttonid.contains(v.getId())){
								
									curimage =  Bitmap.createBitmap(mpic, x*mwidth/dif, y*mheight/dif, mwidth/dif, mheight/dif);
									Intent puzzle=new Intent(LayerActivity.this,PuzzleActivity.class);
									puzzle.putExtra("image", curimage);
									puzzle.putExtra("difficulty", dif);
									mbuttonid.add(mbutton[x][y]);
									popped = false;
									startActivityForResult(puzzle,1);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		if(requestCode==1){
			if(resultCode==RESULT_OK){
				boolean comp=data.getBooleanExtra("COMPLETE", false);
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
}