/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package atorrentapi;

import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import android.util.Log;

/**
 * 
 * Class enabling to process a torrent file
 * 
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class TorrentProcessor {

	private TorrentFile torrent;

	public TorrentProcessor(TorrentFile torrent) {
		this.torrent = torrent;
	}

	public TorrentProcessor() {
		this.torrent = new TorrentFile();
	}

	/**
	 * Given the path of a torrent, parse the file and represent it as a Map
	 * 
	 * @param filename
	 *            String
	 * @return Map
	 */
	public Map parseTorrent(String filename) {
		return this.parseTorrent(new File(filename));
	}

	/**
	 * Given a File (supposed to be a torrent), parse it and represent it as a
	 * Map
	 * 
	 * @param file
	 *            File
	 * @return Map
	 */
	public Map parseTorrent(File file) {
		try {
			return BDecoder.decode(IOManager.readBytesFromFile(file));
		} catch (IOException ioe) {
		}
		return null;
	}

	protected String readStringFromMetaData(Map meta_data, String name)

	{
		Object obj = meta_data.get(name);

		if (obj instanceof byte[]) {

			return (readStringFromMetaData((byte[]) obj));
		}

		return (null);
	}

	protected String readStringFromMetaData(byte[] value)

	{
		if (value == null) {

			return (null);
		}

		return (new String(value));
	}

	/**
	 * Given a Map, retrieve all useful information and represent it as a
	 * TorrentFile object
	 * 
	 * @param m
	 *            Map
	 * @return TorrentFile
	 */
	@SuppressWarnings("unchecked")
	public TorrentFile getTorrentFile(Map m) {
		Vector<URL> urls = new Vector<URL>();
		String TK_ANNOUNCE = "announce";
		String TK_ANNOUNCE_LIST = "announce-list";

		if (m == null)
			return null;

		if (!m.containsKey(TK_ANNOUNCE) && !m.containsKey(TK_ANNOUNCE_LIST))
			return null;

		boolean announce_url_found = false;
		List announce_list = null;
		String announce_url = null;

		
		
		//they may only have one tracker
		if (m.containsKey(TK_ANNOUNCE)) // mandatory key
		{
			announce_url = new String((byte[]) m.get(TK_ANNOUNCE));
			if (announce_url != null) {
				announce_url = announce_url.replaceAll(" ", "");
				this.torrent.announceURL = announce_url;
				
			
				//Log.v("AndroidTorrent","adding url " +announce_url);
				
				
				try {
					urls.add(new URL(announce_url));
					this.torrent.announceURLS = (Vector<URL>) urls.clone();
					
					int urlssize=this.torrent.announceURLS.size();
					//Log.v("androidtorrent","we have "+urlssize+" trackers");
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		
		
		if (m.containsKey(TK_ANNOUNCE_LIST) || m.containsKey(TK_ANNOUNCE) ) {

			boolean got_announce_list = true;

			Object ann_list = m.get(TK_ANNOUNCE_LIST);

			if (ann_list instanceof List) { // some malformed torrents have this
											// key as a zero-sized string
											// instead of a zero-sized list
				announce_list = (List) ann_list;
			}

			if (announce_list != null && announce_list.size() > 0) {

				for (int i = 0; i < announce_list.size(); i++) {

					List set = (List) announce_list.get(i);

					

					for (int j = 0; j < set.size(); j++) {

						String url_str = readStringFromMetaData((byte[]) set
								.get(j));

						url_str = url_str.replaceAll(" ", "");

						// check to see if the announce url is somewhere in the
						// announce-list

						try {
							urls.add(new URL(url_str));

							if (url_str.equalsIgnoreCase(announce_url)) {

								announce_url_found = true;
							}

						} catch (MalformedURLException e) {

							if (url_str.indexOf("://") == -1) {

								url_str = "http:/"
										+ (url_str.startsWith("/") ? "" : "/")
										+ url_str;
							}

							try {
								urls.add(new URL(url_str));

								if (url_str.equalsIgnoreCase(announce_url)) {

									announce_url_found = true;
								}

							} catch (MalformedURLException f) {
								System.err
										.println("MalformedURLException! - read tracker list  url = " +url_str);
								// Debug.printStackTrace( f );
							}
						}
					}

					// if the original announce url isn't found, add it to the
					// list
					// watch out for those invalid torrents with announce url
					// missing
					if (!announce_url_found && announce_url != null && announce_url.length() > 0) {
						try {
							//Log.v("AndroidTorrent","adding to urls " +announce_url);
							urls.add(new URL(announce_url));
							URL[] url_array = new URL[urls.size()];
							urls.copyInto(url_array);
							//addTorrentAnnounceURLSet(url_array);
							this.torrent.announceURLS = (Vector<URL>) urls.clone();
					  		this.torrent.totalurlid++;
					  		
					  		int urlssize=this.torrent.announceURLS.size();
							//Log.v("androidtorrent","we have "+urlssize+" trackers");
					  		
					  		
					  		
						} catch (Exception e) {
							System.err
									.println("exception na leitura do .torrent");
						}
					}

					if (urls.size() > 0) {

					   // URL[] url_array = new URL[urls.size()];
						this.torrent.announceURLS = (Vector<URL>) urls.clone();
						
						int urlssize=this.torrent.announceURLS.size();
						
						//Log.v("androidtorrent","we have "+urlssize+" trackers");
						
						//if the size is greater than 1 and totalurlid is less than urlssize
						if(urlssize>1 && this.torrent.totalurlid <= urlssize){
							this.torrent.totalurlid++;
						}

						 //urls.copyInto( url_array );

						 //addTorrentAnnounceURLSet( url_array );
					}

				}
			}

		}
		;

		if (m.containsKey("comment")) // optional key
			this.torrent.comment = new String((byte[]) m.get("comment"));
		if (m.containsKey("created by")) // optional key
			this.torrent.createdBy = new String((byte[]) m.get("created by"));
		if (m.containsKey("creation date")) // optional key
			this.torrent.creationDate = (Long) m.get("creation date");
		if (m.containsKey("encoding")) // optional key
			this.torrent.encoding = new String((byte[]) m.get("encoding"));

		// Store the info field data
		if (m.containsKey("info")) {
			Map info = (Map) m.get("info");
			try {

				this.torrent.info_hash_as_binary = Utils.hash(BEncoder
						.encode(info));

				// BitTorrent API
				// this.torrent.info_hash_as_hex = Utils.byteArrayToByteString(
				// this.torrent.info_hash_as_binary);

				this.torrent.info_hash_as_url = Utils
						.byteArrayToURLString(this.torrent.info_hash_as_binary);

				String encoding = "ISO-8859-1";
				// String encoding = "UTF-8" ;
				// String encoding = "UTF-16";

				// Metodo limewire
				String shash1 = new String( // (2) Convert that into a String
											// using normal ASCII encoding
						this.torrent.info_hash_as_binary, encoding // (1) get
																	// the SHA1
																	// hash of
																	// the
																	// "info"
																	// section
																	// of the
																	// .torrent
																	// file in a
																	// 20-byte
																	// array
				);

				String infoHash = URLEncoder.encode( // (3) Encode unsafe bytes
														// for the Web, creating
														// a bunch of percents
														// like %20
						shash1, encoding);

				this.torrent.info_hash_as_url = infoHash;

			} catch (IOException ioe) {
				return null;
			}
			if (info.containsKey("name"))
				this.torrent.saveAs = new String((byte[]) info.get("name"));
			if (info.containsKey("piece length"))
				this.torrent.pieceLength = ((Long) info.get("piece length"))
						.intValue();
			else
				return null;

			if (info.containsKey("pieces")) {
				byte[] piecesHash2 = (byte[]) info.get("pieces");
				if (piecesHash2.length % 20 != 0)
					return null;

				for (int i = 0; i < piecesHash2.length / 20; i++) {
					byte[] temp = Utils.subArray(piecesHash2, i * 20, 20);
					this.torrent.piece_hash_values_as_binary.add(temp);
					this.torrent.piece_hash_values_as_hex.add(Utils
							.byteArrayToByteString(temp));
					this.torrent.piece_hash_values_as_url.add(Utils
							.byteArrayToURLString(temp));
				}
			} else
				return null;

			if (info.containsKey("files")) {
				List multFiles = (List) info.get("files");
				this.torrent.total_length = 0;
				for (int i = 0; i < multFiles.size(); i++) {
					this.torrent.length.add(((Long) ((Map) multFiles.get(i))
							.get("length")).intValue());
					this.torrent.total_length += ((Long) ((Map) multFiles
							.get(i)).get("length")).intValue();

					List path = (List) ((Map) multFiles.get(i)).get("path");
					String filePath = "";
					for (int j = 0; j < path.size(); j++) {
						filePath += new String((byte[]) path.get(j));
					}
					this.torrent.name.add(filePath);
				}
			} else {
				this.torrent.length.add(((Long) info.get("length")).intValue());
				this.torrent.total_length = ((Long) info.get("length"))
						.intValue();
				this.torrent.name.add(new String((byte[]) info.get("name")));
			}
		} else
			return null;
		return this.torrent;
	}

	

	/**
	 * Sets the TorrentFile object of the Publisher equals to the given one
	 * 
	 * @param torr
	 *            TorrentFile
	 */
	public void setTorrent(TorrentFile torr) {
		this.torrent = torr;
	}

	/**
	 * Updates the TorrentFile object according to the given parameters
	 * 
	 * @param url
	 *            The announce url
	 * @param pLength
	 *            The length of the pieces of the torrent
	 * @param comment
	 *            The comments for the torrent
	 * @param encoding
	 *            The encoding of the torrent
	 * @param filename
	 *            The path of the file to be added to the torrent
	 */
	public void setTorrentData(String url, int pLength, String comment,
			String encoding, String filename) {
		this.torrent.announceURL = url;
		this.torrent.pieceLength = pLength * 1024;
		this.torrent.createdBy = Constants.CLIENT;
		this.torrent.comment = comment;
		this.torrent.creationDate = System.currentTimeMillis();
		this.torrent.encoding = encoding;
		this.addFile(filename);
	}

	/**
	 * Updates the TorrentFile object according to the given parameters
	 * 
	 * @param url
	 *            The announce url
	 * @param pLength
	 *            The length of the pieces of the torrent
	 * @param comment
	 *            The comments for the torrent
	 * @param encoding
	 *            The encoding of the torrent
	 * @param name
	 *            The name of the directory to save the files in
	 * @param filenames
	 *            The path of the file to be added to the torrent
	 * @throws java.lang.Exception
	 */
	public void setTorrentData(String url, int pLength, String comment,
			String encoding, String name, List filenames) throws Exception {
		this.torrent.announceURL = url;
		this.torrent.pieceLength = pLength * 1024;
		this.torrent.comment = comment;
		this.torrent.createdBy = Constants.CLIENT;
		this.torrent.creationDate = System.currentTimeMillis();
		this.torrent.encoding = encoding;
		this.torrent.saveAs = name;
		this.addFiles(filenames);
	}

	/**
	 * Sets the announce url of the torrent
	 * 
	 * @param url
	 *            String
	 */
	public void setAnnounceURL(String url) {
		this.torrent.announceURL = url;
	}

	/**
	 * Sets the pieceLength
	 * 
	 * @param length
	 *            int
	 */
	public void setPieceLength(int length) {
		this.torrent.pieceLength = length * 1024;
	}

	/**
	 * Sets the directory the files have to be saved in (in case of multiple
	 * files torrent)
	 * 
	 * @param name
	 *            String
	 */
	public void setName(String name) {
		this.torrent.saveAs = name;
	}

	/**
	 * Sets the comment about this torrent
	 * 
	 * @param comment
	 *            String
	 */
	public void setComment(String comment) {
		this.torrent.comment = comment;
	}

	/**
	 * Sets the creator of the torrent. This should be the client name and
	 * version
	 * 
	 * @param creator
	 *            String
	 */
	public void setCreator(String creator) {
		this.torrent.createdBy = creator;
	}

	/**
	 * Sets the time the torrent was created
	 * 
	 * @param date
	 *            long
	 */
	public void setCreationDate(long date) {
		this.torrent.creationDate = date;
	}

	/**
	 * Sets the encoding of the torrent
	 * 
	 * @param encoding
	 *            String
	 */
	public void setEncoding(String encoding) {
		this.torrent.encoding = encoding;
	}

	/**
	 * Add the files in the list to the torrent
	 * 
	 * @param l
	 *            A list containing the File or String object representing the
	 *            files to be added
	 * @return int The number of files that have been added
	 * @throws Exception
	 */
	public int addFiles(List l) throws Exception {
		return this.addFiles(l.toArray());
	}

	/**
	 * Add the files in the list to the torrent
	 * 
	 * @param file
	 *            The file to be added
	 * @return int The number of file that have been added
	 * @throws Exception
	 */
	public int addFile(File file) {
		return this.addFiles(new File[] { file });
	}

	/**
	 * Add the files in the list to the torrent
	 * 
	 * @param filename
	 *            The path of the file to be added
	 * @return int The number of file that have been added
	 * @throws Exception
	 */
	public int addFile(String filename) {
		return this.addFiles(new String[] { filename });
	}

	/**
	 * Add the files in the list to the torrent
	 * 
	 * @param filenames
	 *            An array containing the files to be added
	 * @return int The number of files that have been added
	 * @throws Exception
	 */
	public int addFiles(Object[] filenames) {
		int nbFileAdded = 0;

		if (this.torrent.total_length == -1)
			this.torrent.total_length = 0;

		for (int i = 0; i < filenames.length; i++) {
			File f = null;
			if (filenames[i] instanceof String)
				f = new File((String) filenames[i]);
			else if (filenames[i] instanceof File)
				f = (File) filenames[i];
			if (f != null)
				if (f.exists()) {
					this.torrent.total_length += f.length();
					this.torrent.name.add(f.getPath());
					this.torrent.length.add(new Long(f.length()).intValue());
					nbFileAdded++;
				}
		}
		return nbFileAdded;
	}

	/**
	 * Generate the SHA-1 hashes for the file in the torrent in parameter
	 * 
	 * @param torr
	 *            TorrentFile
	 */
	public void generatePieceHashes(TorrentFile torr) {
		ByteBuffer bb = ByteBuffer.allocate(torr.pieceLength);
		int index = 0;
		long total = 0;
		torr.piece_hash_values_as_binary.clear();
		for (int i = 0; i < torr.name.size(); i++) {
			total += (Integer) torr.length.get(i);
			File f = new File((String) torr.name.get(i));
			if (f.exists()) {
				try {
					FileInputStream fis = new FileInputStream(f);
					int read = 0;
					byte[] data = new byte[torr.pieceLength];
					while ((read = fis.read(data, 0, bb.remaining())) != -1) {
						bb.put(data, 0, read);
						if (bb.remaining() == 0) {
							torr.piece_hash_values_as_binary.add(Utils.hash(bb
									.array()));
							bb.clear();
						}
					}
				} catch (FileNotFoundException fnfe) {
				} catch (IOException ioe) {
				}
			}
		}
		if (bb.remaining() != bb.capacity())
			torr.piece_hash_values_as_binary.add(Utils.hash(Utils.subArray(bb
					.array(), 0, bb.capacity() - bb.remaining())));
	}

	/**
	 * Generate the SHA-1 hashes for the files in the current object TorrentFile
	 */
	public void generatePieceHashes() {
		this.generatePieceHashes(this.torrent);
	}

	/**
	 * Generate the bytes of the bencoded TorrentFile data
	 * 
	 * @param torr
	 *            TorrentFile
	 * @return byte[]
	 */
	public byte[] generateTorrent(TorrentFile torr) {
		SortedMap map = new TreeMap();
		map.put("announce", torr.announceURL);
		if (torr.comment.length() > 0)
			map.put("comment", torr.comment);
		if (torr.creationDate >= 0)
			map.put("creation date", torr.creationDate);
		if (torr.createdBy.length() > 0)
			map.put("created by", torr.createdBy);

		SortedMap info = new TreeMap();
		if (torr.name.size() == 1) {
			info.put("length", (Integer) torr.length.get(0));
			info.put("name", new File((String) torr.name.get(0)).getName());
		} else {
			if (!torr.saveAs.matches(""))
				info.put("name", torr.saveAs);
			else
				info.put("name", "noDirSpec");
			ArrayList files = new ArrayList();
			for (int i = 0; i < torr.name.size(); i++) {
				SortedMap file = new TreeMap();
				file.put("length", (Integer) torr.length.get(i));
				String[] path = ((String) torr.name.get(i)).split("\\\\");
				File f = new File((String) (torr.name.get(i)));

				ArrayList pathList = new ArrayList(path.length);
				for (int j = (path.length > 1) ? 1 : 0; j < path.length; j++)
					pathList.add(path[j]);
				file.put("path", pathList);
				files.add(file);
			}
			info.put("files", files);
		}
		info.put("piece length", torr.pieceLength);
		byte[] pieces = new byte[0];
		for (int i = 0; i < torr.piece_hash_values_as_binary.size(); i++)
			pieces = Utils.concat(pieces,
					(byte[]) torr.piece_hash_values_as_binary.get(i));
		info.put("pieces", pieces);
		map.put("info", info);
		try {
			byte[] data = BEncoder.encode(map);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Generate the bytes for the current object TorrentFile
	 * 
	 * @return byte[]
	 */
	public byte[] generateTorrent() {
		return this.generateTorrent(this.torrent);
	}

	/**
	 * Returns the local TorrentFile in its current state
	 * 
	 * @return TorrentFile
	 */
	public TorrentFile getTorrent() {
		return this.torrent;
	}

}
