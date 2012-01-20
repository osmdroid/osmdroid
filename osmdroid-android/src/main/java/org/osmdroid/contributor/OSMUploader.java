package org.osmdroid.contributor;

/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 *
 * Original JAVA-Code ported for Android compatibility by Nicolas 'plusminus' Gramlich.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.contributor.util.RecordedRouteGPXFormatter;
import org.osmdroid.contributor.util.Util;
import org.osmdroid.contributor.util.constants.OpenStreetMapContributorConstants;

/**
 * Small java class that allows to upload gpx files to www.openstreetmap.org via its api call.
 * 
 * @author cdaller
 * @author Nicolas Gramlich
 */
public class OSMUploader implements OpenStreetMapContributorConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String API_VERSION = "0.5";
	private static final int BUFFER_SIZE = 65535;
	private static final String BASE64_ENC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String BOUNDARY = "----------------------------d10f7aa230e8";
	private static final String LINE_END = "\r\n";

	private static final String DEFAULT_DESCRIPTION = "AndNav - automatically created route.";
	private static final String DEFAULT_TAGS = "AndNav";

	public static final SimpleDateFormat pseudoFileNameFormat = new SimpleDateFormat(
			"yyyyMMdd'_'HHmmss'_'SSS");
	private static final SimpleDateFormat autoTagFormat = new SimpleDateFormat("MMMM yyyy");

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * This is a utility class with only static members.
	 */
	private OSMUploader() {
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Uses OSMConstants.OSM_USERNAME and OSMConstants.OSM_PASSWORD as username/password.
	 * Description will be <code>DEFAULT_DESCRIPTION</code>, tags will be automatically generated
	 * (i.e. "<code>October 2008</code>") NOTE: This method is not blocking!
	 * 
	 * @param gpxInputStream
	 *            the InputStream containing the gpx-data.
	 * @throws IOException
	 */
	public static void uploadAsync(final ArrayList<RecordedGeoPoint> recordedGeoPoints) {
		uploadAsync(DEFAULT_DESCRIPTION, DEFAULT_TAGS, true, recordedGeoPoints);
	}

	/**
	 * Uses OSMConstants.OSM_USERNAME and OSMConstants.OSM_PASSWORD as username/password. The
	 * 'filename' will be the current <code>timestamp.gpx</code> (i.e. "20081231_234815_912.gpx")
	 * NOTE: This method is not blocking!
	 * 
	 * @param description
	 *            <code>not null</code>
	 * @param tags
	 *            <code>not null</code>
	 * @param addDateTags
	 *            adds Date Tags to the existing Tags (i.e. "October 2008")
	 * @param gpxInputStreaman
	 *            the InputStream containing the gpx-data.
	 * @throws IOException
	 */
	public static void uploadAsync(final String description, final String tags,
			final boolean addDateTags, final ArrayList<RecordedGeoPoint> recordedGeoPoints) {
		uploadAsync(OSM_USERNAME, OSM_PASSWORD, description, tags, addDateTags, recordedGeoPoints,
				pseudoFileNameFormat.format(new GregorianCalendar().getTime()) + "_" + OSM_USERNAME
						+ ".gpx");
	}

	/**
	 * NOTE: This method is not blocking! (Code runs in thread)
	 * 
	 * @param username
	 *            <code>not null</code> and <code>not empty</code>. Valid OSM-username
	 * @param password
	 *            <code>not null</code> and <code>not empty</code>. Valid password to the
	 *            OSM-username.
	 * @param description
	 *            <code>not null</code>
	 * @param tags
	 *            if <code>null</code> addDateTags is treated as <code>true</code>
	 * @param addDateTags
	 *            adds Date Tags to the existing Tags (i.e. "<code>October 2008</code>")
	 * @param gpxInputStream
	 *            the InputStream containing the gpx-data.
	 * @param pseudoFileName
	 *            ending with "<code>.gpx</code>"
	 * @throws IOException
	 */
	public static void uploadAsync(final String username, final String password,
			final String description, final String tags, final boolean addDateTags,
			final ArrayList<RecordedGeoPoint> recordedGeoPoints, final String pseudoFileName) {
		if (username == null || username.length() == 0)
			return;
		if (password == null || password.length() == 0)
			return;
		if (description == null || description.length() == 0)
			return;
		if (tags == null || tags.length() == 0)
			return;
		if (pseudoFileName == null || pseudoFileName.endsWith(".gpx"))
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!Util.isSufficienDataForUpload(recordedGeoPoints))
					return;

				final InputStream gpxInputStream = new ByteArrayInputStream(
						RecordedRouteGPXFormatter.create(recordedGeoPoints).getBytes());

				String tagsToUse = tags;
				if (addDateTags || tagsToUse == null)
					if (tagsToUse == null)
						tagsToUse = autoTagFormat.format(new GregorianCalendar().getTime());
					else
						tagsToUse = tagsToUse + " "
								+ autoTagFormat.format(new GregorianCalendar().getTime());

				// logger.debug("Uploading " + pseudoFileName + " to openstreetmap.org");
				try {
					// String urlGpxName = URLEncoder.encode(gpxName.replaceAll("\\.;&?,/","_"),
					// "UTF-8");
					final String urlDesc = (description == null) ? DEFAULT_DESCRIPTION
							: description.replaceAll("\\.;&?,/", "_");
					final String urlTags = (tagsToUse == null) ? DEFAULT_TAGS
							: tagsToUse.replaceAll("\\\\.;&?,/", "_");
					final URL url = new URL("http://www.openstreetmap.org/api/" + API_VERSION
							+ "/gpx/create");
					// logger.debug("Destination Url: " + url);
					final HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setConnectTimeout(15000);
					con.setRequestMethod("POST");
					con.setDoOutput(true);
					con.addRequestProperty("Authorization", "Basic "
							+ encodeBase64(username + ":" + password));
					con.addRequestProperty("Content-Type", "multipart/form-data; boundary="
							+ BOUNDARY);
					con.addRequestProperty("Connection", "close"); // counterpart of keep-alive
					con.addRequestProperty("Expect", "");

					con.connect();
					final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
							con.getOutputStream()));
					// DataOutputStream out = new DataOutputStream(System.out);

					writeContentDispositionFile(out, "file", gpxInputStream, pseudoFileName);
					writeContentDisposition(out, "description", urlDesc);
					writeContentDisposition(out, "tags", urlTags);

					writeContentDisposition(out, "public", "1");

					out.writeBytes("--" + BOUNDARY + "--" + LINE_END);
					out.flush();

					final int retCode = con.getResponseCode();
					String retMsg = con.getResponseMessage();
					// logger.debug("\nreturn code: "+retCode + " " + retMsg);
					if (retCode != 200) {
						// Look for a detailed error message from the server
						if (con.getHeaderField("Error") != null)
							retMsg += "\n" + con.getHeaderField("Error");
						con.disconnect();
						throw new RuntimeException(retCode + " " + retMsg);
					}
					out.close();
					con.disconnect();
				} catch (final Exception e) {
					// logger.error("OSMUpload Error", e);
				}
			}

		}, "OSMUpload-Thread").start();
	}

	public static void upload(final String username, final String password,
			final String description, final String tags, final boolean addDateTags,
			final ArrayList<RecordedGeoPoint> recordedGeoPoints, final String pseudoFileName)
			throws IOException {
		uploadAsync(username, password, description, tags, addDateTags, recordedGeoPoints,
				pseudoFileName);
	}

	/**
	 * @param out
	 * @param string
	 * @param gpxFile
	 * @throws IOException
	 */
	private static void writeContentDispositionFile(final DataOutputStream out, final String name,
			final InputStream gpxInputStream, final String pseudoFileName) throws IOException {
		out.writeBytes("--" + BOUNDARY + LINE_END);
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
				+ pseudoFileName + "\"" + LINE_END);
		out.writeBytes("Content-Type: application/octet-stream" + LINE_END);
		out.writeBytes(LINE_END);

		final byte[] buffer = new byte[BUFFER_SIZE];
		// int fileLen = (int)gpxFile.length();
		int read;
		int sumread = 0;
		final InputStream in = new BufferedInputStream(gpxInputStream);
		// logger.debug("Transferring data to server");
		while ((read = in.read(buffer)) >= 0) {
			out.write(buffer, 0, read);
			out.flush();
			sumread += read;
		}
		in.close();
		out.writeBytes(LINE_END);
	}

	/**
	 * @param string
	 * @param urlDesc
	 * @throws IOException
	 */
	private static void writeContentDisposition(final DataOutputStream out, final String name,
			final String value) throws IOException {
		out.writeBytes("--" + BOUNDARY + LINE_END);
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
		out.writeBytes(LINE_END);
		out.writeBytes(value + LINE_END);
	}

	private static String encodeBase64(final String s) {
		final StringBuilder out = new StringBuilder();
		for (int i = 0; i < (s.length() + 2) / 3; ++i) {
			final int l = Math.min(3, s.length() - i * 3);
			final String buf = s.substring(i * 3, i * 3 + l);
			out.append(BASE64_ENC.charAt(buf.charAt(0) >> 2));
			out.append(BASE64_ENC.charAt((buf.charAt(0) & 0x03) << 4
					| (l == 1 ? 0 : (buf.charAt(1) & 0xf0) >> 4)));
			out.append(l > 1 ? BASE64_ENC.charAt((buf.charAt(1) & 0x0f) << 2
					| (l == 2 ? 0 : (buf.charAt(2) & 0xc0) >> 6)) : '=');
			out.append(l > 2 ? BASE64_ENC.charAt(buf.charAt(2) & 0x3f) : '=');
		}
		return out.toString();
	}
}