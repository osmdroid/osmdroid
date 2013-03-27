<?php
	function endsWith( $str, $sub ) {
		return ( substr( $str, strlen( $str ) - strlen( $sub ) ) == $sub );
	}
	
	$ERRCODE_NONE = 0;
	$ERRCODE_FILESIZE_EXCEEDED = 1;
	$ERRCODE_FILEENDING_BAD = 2;
	$ERRCODE_FILETYPE_DISALLOWED = 3;
	$ERRCODE_UPLOAD_ERROR = 4;

	$target = "data/";
	$filename = basename( $_FILES['gpxfile']['name']);
	$target = $target . $filename;
	$errorCode = $ERRCODE_NONE;
	
	$xmlHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	$baseErrorTag = "<error errorCode=\"%d\" message=\"%s\"/>";
	$baseSuccessTag = "<success message=\"%s\"/>";
	$errorTag = "";
	$successTag = "";
	
	$uploaded_size =  $_FILES['gpxfile']['size'];

	$maxFilesize = 500000; // In Bytes
	
	//This is our size condition
	if ($errorCode == $ERRCODE_NONE && $uploaded_size > $maxFilesize) {
		$errorCode = $ERRCODE_FILESIZE_EXCEEDED;
		$errorTag = sprintf($baseErrorTag, $errorCode, "Filezise exceeded. File: '" . $uploaded_size . "'  Max: '" . $maxFilesize . "'");
	}
	
	if ($errorCode == $ERRCODE_NONE && !endsWith($filename, ".gpx")) {
		$errorCode = $ERRCODE_FILEENDING_BAD;
		$errorTag = sprintf($baseErrorTag, $errorCode, "Bad file-ending.");
	}

	//This is our limit file type condition
	if ($errorCode == $ERRCODE_NONE && $uploaded_type == "text/php") {
		$errorCode = $ERRCODE_FILETYPE_DISALLOWED;
		$errorTag = sprintf($baseErrorTag, $errorCode, "Disallowed Filetype");
	}

	if ($errorCode == $ERRCODE_NONE) { //If everything is ok we try to upload it
		if(move_uploaded_file($_FILES['gpxfile']['tmp_name'], $target)) {
			$successTag = sprintf($baseSuccessTag, "The file '". $filename . "' has been uploaded");
		} else {
			$errorCode = $ERRCODE_UPLOAD_ERROR;
			$errorTag = sprintf($baseErrorTag, $errorCode, "File '" . $filename . "' could not be uploaded.");
		}
	}
	
	if($errorCode == $ERRCODE_NONE){
		echo $xmlHead . $successTag;
	} else {
		echo $xmlHead . $errorTag;
	}
?> 