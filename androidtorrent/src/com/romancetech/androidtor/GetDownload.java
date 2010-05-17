package com.romancetech.androidtor;

import atorrentapi.Constants;
import atorrentapi.DownloadManager;
import atorrentapi.TorrentFile;
import atorrentapi.TorrentProcessor;
import atorrentapi.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class GetDownload extends Activity implements Runnable {

	private String thetorrent;
	private String torrent;
	private int done = 0;
	private static final int PROGRESS = 0x1;

	private ProgressBar mProgress;
	private ProgressBar mProgress1;
	private ProgressBar spinner;
	private ProgressBar mPeers;
	private ProgressBar mRate;
	private int mProgressStatus = 0;
	private int mProgressStatus1 = 0;
	private int mPeersstatus = 0;

	private ProgressBar mConnected;
	private int mConnectedstatus = 0;
	private RandomAccessFile[] output_files;
	private float TOTAL = 0;
	private int PEERS;
	private int CONNECTED;
	private float RATE;
	private float RATEUP;
	private int PIECES = 0;
	private String comment;
	private int mRatestatus = 0;
	private ProgressBar mFile;
	private int mFilestatus = 0;
	private Handler mHandler = new Handler();
	private int COMPLETEDPIECES = 0;
	public int[] arr = new int[10];
	private static final long MEGABYTE = 1024 * 1024;
	private static final int EXIT = 0;
	private int nbOfFiles = 0;
	private TorrentFile t;
	public DownloadManager dm;
	private Button closebutton;
	public int dlcontinue = 0;
	public int n;
	public TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// need to override the saved instance so they come back here untill the
		// download is done.
		super.onCreate(savedInstanceState);
		
		
		
		
		  


		boolean exists = false;

		// get torrent file from intent
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			thetorrent = extras.getString("torrent");
			// Log.v("Adroidtor", "torrent=" + torrent);
		} else {
			String thetorrent;
		}
		if (thetorrent != null) {
			File file = new File(thetorrent);
			exists = file.exists();
		}
		if (!exists) {
			
			//change view
			
			setContentView(R.layout.error);
			
			TextView text = (TextView) findViewById(R.id.TextView01);// header
			text.setText("No file selected. You can download a .torrent file from the web and then open it from the SDCard, or the browsers download manager.All downloads will be in the AndroidTorrent folder on your SDCard.");
		} else {
			
			
				
			
			
			
			setContentView(R.layout.main2);
			TextView text = (TextView) findViewById(R.id.TextView01);// header
			TextView text2 = (TextView) findViewById(R.id.TextView02);// filename
			// TextView text3 = (TextView)findViewById(R.id.TextView03); //file
			// size
			// TextView text4 = (TextView)findViewById(R.id.TextView04); //file
			// size
			closebutton = (Button) this.findViewById(R.id.Button01);
			text.setText("File Size:");

			// get the contents of thetorrent into a string

			// get all the tracker urls here, and put them into an array of some
			// sort

			// when a tracker fails, remove it from the array and cycle through
			// them on every connect in PeerUpdater ?

			// Log.v("AndroidTor", "thetorrent=" + thetorrent);

			// Log.v("andtor", "torrent  =" + thetorrent);

			TorrentProcessor tp = new TorrentProcessor();
			t = tp.getTorrentFile(tp.parseTorrent(thetorrent));

			//t.printData(false);

			// here's where we get the data
			final long total_length = t.total_length;
			ArrayList<?> name = t.name;

			long megs = total_length / MEGABYTE;

			text.setText("FileSize: " + megs + " M");
			// Log.v("AndroidTor", "total_length=" + total_length);
			int namecount = name.size();

			// Log.v("AndroidTor", "name=" + name);
			text2.setText("Downloading : " + namecount + " files\n \n");

			// Log.v("AndroidTorrent","doing work?");

			File path = Environment.getExternalStorageDirectory();
			Constants.SAVEPATH = path + "/AndroidTorrent/";
			// text.setText("Starting download manager");
			CONNECTED = 0;

			// progressbar
			mProgress = (ProgressBar) findViewById(R.id.ProgressBar);
			// peer indicator
			mPeers = (ProgressBar) findViewById(R.id.Peers);
			// connected indicator
			// rate
			final int v = 0;
			mRate = (ProgressBar) findViewById(R.id.Rate);

			// System.out.println("Trying to start thread");

			final Thread downloadthread = new Thread(this);
			downloadthread.start();
			// System.out.println("Thread should be started now");

			closebutton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Log.v("AndroidTor", "Button Clicked");
					System.exit(1);
					tabHost.setCurrentTab(0);
					// exit and return to tab 0 ?

				}
			});

		}// end else if for card check

	}// end function on create

	public void run() {
		// Log.v("AndroidTor", "Starting download thread");
		Looper.prepare();
		// Log.v("AndroidTor", "running thread startDownload");

		dm = new DownloadManager(t, Utils.generateID(), dlcontinue);

		handler.sendEmptyMessage(0);
		PIECES = dm.nbPieces;
		//Log.v("AndroidTor", "PIECES=" + PIECES);

		// generate a random number 5 digit number
		Random generator = new Random();
		n = generator.nextInt(12343);

		int startport = n;
		int endport = (n + 1000);

		dm.startListening(startport, endport);
		// Log.v("AndroidTor", "started listening");
		dm.startTrackerUpdate();
		// Log.v("AndroidTor", "Now we're connected");
		CONNECTED = 100;
		// Log.v("AndroidTor", "Unchoke 1");
		dm.unchokePeers();
		// Log.v("AndroidTor", "Unchoke 1 done");
		TOTAL = 0;
		RATE = 0;

		byte[] b = new byte[0];
		int x = 0;
		while (true) {
			try {
				synchronized (b) {
					b.wait(1000);
					TOTAL = dm.totaldl;
					RATE = dm.totalrate;

					PEERS = dm.connectedpeers;
					COMPLETEDPIECES = dm.totalcomplete;

					// Log.v("AndroidTor","total="+TOTAL);
					// Log.v("AndroidTor","rate="+RATE);
					// Log.v("AndroidTor","RATEUP="+RATEUP);
					// Log.v("AndroidTor","PEERS="+PEERS);
					handler.sendEmptyMessage(0);

					if (x == 100) {
						//Log.v("AndroidTor", "unchoke");
						dm.unchokePeers();

						b.notifyAll();
						x = 0;
					}
					x++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dm.isComplete())
				imdone();
		}
		// send an update message

		/*
		 * while (TOTAL < 100) { // Update the progress bar mHandler.post(new
		 * Runnable() { public void run() { TOTAL=dm.totaldownload; RATE=
		 * dm.totalrate; RATEUP= dm.currentrateup; PEERS=dm.connectedpeers;
		 * 
		 * TextView ratetext = (TextView)findViewById(R.id.ratetextdown); //file
		 * size //ratetext.setText("Rate Down "+RATE+" k/s  ETA:");
		 * 
		 * TextView pieces = (TextView)findViewById(R.id.pieces); //file size
		 * pieces.setText(COMPLETEDPIECES +" pieces done out of "+PIECES);
		 * 
		 * 
		 * TextView warning = (TextView)findViewById(R.id.TextView06);
		 * warning.setText("Now downloading : "+TOTAL+"%");
		 * 
		 * 
		 * TextView text4 = (TextView)findViewById(R.id.TextView04); //files
		 * written text4.setText(" ");
		 * 
		 * 
		 * mProgress.setProgress((int) TOTAL); TextView text3 =
		 * (TextView)findViewById(R.id.TextView03); //file size
		 * text3.setText(PEERS+" Total available peers");
		 * 
		 * 
		 * } }); }
		 */

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// System.out.println("Message is "+msg);

			//TextView ratetext = (TextView) findViewById(R.id.ratetextdown); // file
			// size

			// round rate ?
			//float showrate = RATE / MEGABYTE;
			//ratetext.setText("Average Rate Down " + RATE + " Kbs ");

			TextView pieces = (TextView) findViewById(R.id.pieces); // file size
			pieces.setText(COMPLETEDPIECES + " pieces done out of " + PIECES);

			TextView warning = (TextView) findViewById(R.id.TextView06);
			warning.setText("Now downloading : " + TOTAL + "%");

			TextView text4 = (TextView) findViewById(R.id.TextView04); // files
			// written
			text4.setText(" ");

			mProgress.setProgress((int) TOTAL);
			TextView text3 = (TextView) findViewById(R.id.TextView03); // file
			// size
			text3.setText("Connected to " + PEERS + " peers");

		}
	};

	public void imdone() {
		// start a "done" intent

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = "Torrent done downloading";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = "Torrent completed";
		CharSequence contentText = "Your torrent is done. Please check the /torrents folder on your sdcard!";
		Intent notificationIntent = new Intent(this, DownloadDone.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		final int HELLO_ID = 1;

		mNotificationManager.notify(HELLO_ID, notification);

		// remove downloading bit
		Intent i = new Intent();
		i.setClassName("com.romancetech.androidtor",
				"com.romancetech.androidtor.DownloadDone");
		startActivity(i);

	}

}// end class
