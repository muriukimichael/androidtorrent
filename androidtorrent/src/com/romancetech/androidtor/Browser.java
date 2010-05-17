package com.romancetech.androidtor;




import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class Browser extends ListActivity {
	
	private String[] args;
	private List<String> items = null;
	private List<String> cargs = new ArrayList<String>();
	private TextView text;
	private boolean myBoolean;
    final ArrayList<View> mViews= new ArrayList<View>();
	/** Called when the activity is first created. */
	
	
	
	
	
	
	
   
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	


       	//get file browser
    	//setContentView(R.layout.directory_list);
    	
    	
    	//generateScreen(2);

    	    
	        
	        FileFilter filter = new FileFilter() { 
	            public boolean accept(File pathname) {
	            	//if pathname ends with .torrent, return true
	            	String thefile=pathname.toString();
	            	String [] temp = thefile.split(".torrent");
	                boolean myreturn=false;
					for (int i = 0 ; i < temp.length ; i++) {
	              	  	if(temp[i]!=null){
	              	  		myreturn = true;
	              	  	}else{
	              	  		myreturn=false;
	              	  	}
	                }
	                return myreturn;
	            } 
	        }; 
	        //File test=new File("/sdcard");
	        File test=Environment.getExternalStorageDirectory();
	      
	        
	        if(test.canWrite()){
	        	fill(test.listFiles(filter));
	        }else{
	        	Message acceptMsg = Message.obtain();
	        	new AlertDialog.Builder(this)
	            .setMessage("Cannot read your SD Card.Unmount/Re-insert your card and try again")
	            .setNeutralButton("OK", null)
	            .show();
	        }
        
        
    	
        
        
    }
        
   public void openDownloads(){
	   Intent i = new Intent();
	   i.setClassName("com.romancetech.androidtor", "com.romancetech.androidtor.GetDownload");
       startActivity(i);	 
   }
   
    private void fill(File[] files) {
	    items = new ArrayList<String>();
	    cargs = new ArrayList<String>();
	    items.add(getString(R.string.to_top));
		int x=0;  
		for( File file : files ){
			File currentfile = file;
			String filename=file.toString();
			int istorrent=istorrent(filename);
			//if this file isnt a torrent.. dont pass it
			if(istorrent==1){
			    String myname="";
			    cargs.add(file.getAbsolutePath());
			    items.add("Torrent File: \n"+filename);
			    x++;
			}else{
			    cargs.add(file.getPath());
				items.add(file.getPath());
			}
		}
        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.file_row, items);
        setListAdapter(fileList);
    }
    
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
           int selectionRowID = (int)position;
           if (selectionRowID==0){
               fillWithRoot();
           } else {
               File file = new File(items.get(selectionRowID));
               if (file.isDirectory()){
                   fill(file.listFiles());
                   
               }else{
            	 //if this is a .torrent, open it!
                   if(selectionRowID!=0){
    	               final String thefile=cargs.get(selectionRowID-1);
    	               //System.out.println("Clicked on file "+thefile);
    	               int result=istorrent(thefile);
    	               //start new intent for downloading
    	               if(result==1){
    	            	   
    	            	   
    	            	   
    	            	   //get the title from the torrent?
    	            	   
    	            	   
    	            	   
    	            	  //promt for download ?
    	            	   Builder alert1 = new AlertDialog.Builder(this)
    	                   .setTitle("AndroidTor")
    	                   .setMessage("Do you want to download this torrent?")
    	                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	                           public void onClick(DialogInterface dialog, int whichButton) {
    	                              //Put your code in here for a positive response
    	                        	  Intent i = new Intent();
    	         	             	  i.putExtra("torrent", thefile);
    	         	             	  i.setClassName("com.romancetech.androidtor", "com.romancetech.androidtor.GetDownload");
    	         	             	  startActivity(i);
    	         	             	  
    	         	             	  
    	         	             	  
    	                           }
    	                   })
    	                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	                           public void onClick(DialogInterface dialog, int whichButton) {
    	                                   //Put your code in here for a negative response
    	                        	  
    	                           }
    	                   });
    	            	   
    	            	   alert1.show();
    	            	   
    	            	   
    	            	   
    	            	   
    	             	 
    	             	  
    	             	  
    	             	  
    	               }
                   }
                  
               }
                  
          }
      }
    
   
   private void fillWithRoot() {
	   File file = Environment.getExternalStorageDirectory();
       fill(file.listFiles());
   }
    
   
   
   
   public int istorrent(String file){
	   String searchFor = ".torrent";
	   String base = file;
       int len = searchFor.length();
       int result = 0;
       if (len > 0) {  
       int start = base.indexOf(searchFor);
       while (start != -1) {
                 result++;
                 start = base.indexOf(searchFor, start+len);
             }
         }
       return result;
   }
   
   
   
      
   
   
   
   
   
   
   
   
}

