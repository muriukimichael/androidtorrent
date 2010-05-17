package com.romancetech.androidtor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.sax.Element;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Search extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		// need to override the saved instance so they come back here untill the
		// download is done.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		generateScreen(1);

		findViewById(R.id.Button01).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText searchtext = ((EditText) findViewById(R.id.EditText01));
				String search = searchtext.getText().toString();
				getBrowser(search);
			}

		}

		);

	}

	public void getBrowser(String search) {

		// String myurl="http://isohunt.com/torrents/?ihq="+search;

		// String
		// myurl="http://www.google.com/search?q=filetype%3Atorrent+mp3+"+search;
		String myurl = "http://thepiratebay.org/search/" + search;
		// still need to use iso hunt i think.. maybe piratebay?

		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(myurl)));

	}

	public void findresults(String search) {
		// System.out.println("Searching for "+search);

		// connect to isohunt and get results
		// http://isohunt.com/torrents/?ihq=
		String myurl = "http://isohunt.com/js/rss/" + search + "?iht=";

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document doc = parser.parse(new URL(myurl).openConnection()
					.getInputStream());

			NodeList nl = doc.getElementsByTagName("item");
			int items = nl.getLength();

			for (int i = 0; i < nl.getLength(); i++) {

				// System.out.println(nl.item(i).getNodeName());
				NodeList itemlist = nl.item(i).getChildNodes();
				int children = itemlist.getLength();
				for (int x = 0; x < itemlist.getLength(); x++) {
					System.out.println(itemlist.item(i).getNodeName());
					String name = itemlist.item(i).getNodeName();

				}

			}

		} catch (ParserConfigurationException e) {

			System.out.println("Error" + e.getMessage());
		} catch (FactoryConfigurationError e) {

			System.out.println("Error" + e.getMessage());
		} catch (SAXException e) {

			System.out.println("Error" + e.getMessage());
		} catch (MalformedURLException e) {

			System.out.println("Error" + e.getMessage());
		} catch (IOException e) {

			System.out.println("Error" + e.getMessage());
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/*
	 * generateScreen(int tabNo) is a method which will append the view for
	 * pressed tab to tab_holder. It take an Integer, which is the tab number.
	 * By default I want to show tab1, that’s why generateScreen(1)is called in
	 * onCreate.
	 */

	private void generateScreen(int tabNum) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.content);
		if (tabNum == 1) {
			View vv = View.inflate(Search.this, R.layout.search, null);
			ll.addView(vv, new LinearLayout.LayoutParams(
					ll.getLayoutParams().width, ll.getLayoutParams().height));
		} else if (tabNum == 2) {
			// browse
			Intent i = new Intent();
			i.setClassName("com.romancetech.androidtor",
					"com.romancetech.androidtor.Browser");
			startActivity(i);
		} else if (tabNum == 3) {
			// switch to download intent
			Intent i = new Intent();
			i.setClassName("com.romancetech.androidtor",
					"com.romancetech.androidtor.GetDownload");
			startActivity(i);

		}
		setListeners();
	}

	private void setListeners() {
		// TextView tab1 = (TextView) findViewById(R.id.tab1);
		// tab1.setOnClickListener(onClickListener);
		TextView tab2 = (TextView) findViewById(R.id.tab2);
		tab2.setOnClickListener(onClickListener);
		TextView tab3 = (TextView) findViewById(R.id.tab3);
		tab3.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		public void onClick(final View v) {
			LinearLayout ll = (LinearLayout) findViewById(R.id.content);

			switch (v.getId()) {
			case R.id.tab1:

				System.out.println("tab1   search");

				break;

			case R.id.tab2:
				System.out.println("tab2  search");
				// ll.removeAllViews();
				generateScreen(2);
				break;

			case R.id.tab3:
				System.out.println("tab3");
				generateScreen(3);
				break;
			}
		}

	};

}
