package com.romancetech.androidtor;










import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;



public class andtor extends TabActivity {
	
	//private String[] args;
	//private List<String> items = null;
	//private List<String> cargs = new ArrayList<String>();
	//private TextView text;
	//private boolean myBoolean;
	public Bundle extras;
    /** Called when the activity is first created. */
	
	
	
	
	
	
	
	
   
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	
    	extras = getIntent().getExtras();
    	Uri	torrent=getIntent().getData();
    	Log.v("androidtorrent","torrent is "+torrent);
    	
    	
    	
    	
    	
    	setContentView(R.layout.maintabs);
		Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
     // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Browser.class);
     // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("SelectUser").setIndicator("Browse Files",res.getDrawable(R.drawable.folder))
        		    .setContent(intent);
        tabHost.addTab(spec);

        
        
        // Do the same for the other tabs
        intent = new Intent().setClass(this, GetDownload.class);
        spec = tabHost.newTabSpec("AddUser").setIndicator("Current Download",
        		res.getDrawable(R.drawable.disk))
                      .setContent(intent);
        tabHost.addTab(spec);
    	
        
        
        
        
        
        
    	
    	
    	
    	

    	//if this is from the listener thing
    	
		
    	
    	if(torrent!=null){
    		String thetorrent=torrent.toString();
    		thetorrent=thetorrent.replace("file://","");
    		//start new intent for downloading
    		Intent i = new Intent();
    		i.putExtra("torrent", thetorrent);
    		i.setClassName("com.romancetech.androidtor", "com.romancetech.androidtor.GetDownload");
            startActivity(i);
            Log.v("androidtorrent","there are extras");
            tabHost.setCurrentTab(1);
            
            
             
    	}else{
    		tabHost.setCurrentTab(0);
    		
    		
    	}
        
        
    }

    
    
    
    


     
   
}

