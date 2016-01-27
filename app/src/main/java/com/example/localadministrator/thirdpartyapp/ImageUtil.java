package com.example.localadministrator.thirdpartyapp;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhy
 */
public class ImageUtil {
	/**
	 * get the imagePath in the storage.
	 * @return the list of the imagePath.
	 */
	public ArrayList<String> getImagePath(){
		ArrayList<String> imagePath = new ArrayList<String>();
		String state = Environment.getExternalStorageState();
		System.out.println(state);

		String filePath = "storage/emulated/0/DCIM/Camera";
		File fileAll = new File(filePath);
		File[] files = fileAll.listFiles();
		System.out.println(files.length);
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (checkIsImageFile(file.getPath())) {
				imagePath.add(file.getPath());
			}
		}
		System.out.println("the path is:"+imagePath);
		return imagePath;
	}


	    /**
	     * check the extension name to get the images
	     * @param fName  file name
	     * @return
	     */
	    //@SuppressLint("DefaultLocale")
	    private boolean checkIsImageFile(String fName) {
	        boolean isImageFile = false;
	        // get the extension name;
	        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
	                fName.length()).toLowerCase();  
	        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")  
	                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {  
	            isImageFile = true;  
	        } else {  
	            isImageFile = false;  
	        }  
	        return isImageFile;  
	    }  
}
