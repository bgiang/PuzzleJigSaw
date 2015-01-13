package com.example.photojigsaw;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.util.Log;

public class GameData implements Serializable {
	
	private static final long serialVersionUID = -5377031759562340438L;
	private transient Bitmap image;
	private String key;
	private int difficulty;
	
	private boolean [][] isLocked;
	private boolean [][] isCompleted;
	
	public GameData(Bitmap b, String k, int size, boolean layered) {
		image = b;
		key = k;
		difficulty = size;
		
		if(layered) {
			isLocked = new boolean[size][size];
			isCompleted = new boolean[size][size];
		} else {
			isLocked = null;
			isCompleted = null;
		}
	}
	
	public Bitmap getBitmap() {
		return image;
	}
	
	public String getKey() {
		return key;
	}
	
	public int getDifficulty() {
		return difficulty;
	}
	
	public boolean[][] getIsLocked() {
		return isLocked;
	}
	
	public boolean[][] getIsCompleted() {
		return isCompleted;
	}
	
	public void setBitmap(Bitmap b) {
		image = b;
	}
	
	public void setKey(String k) {
		key = k;
	}
	
	public void setDifficulty(int d) {
		difficulty = d;
	}
	
	public void lock(int row, int col) {
		isLocked[row][col] = true;
	}
	
	public void unlock(int row, int col) {
		isLocked[row][col] = false;
	}
	
	public void complete(int row, int col) {
		isCompleted[row][col] = true;
	}

}
