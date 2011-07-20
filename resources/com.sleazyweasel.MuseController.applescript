tell application id "com.sleazyweasel.MuseController"
	if it is running then
		try
			tell application "URL Access Scripting"
				download "http://localhost:23233/pianobar/airfoilstatusdata" to "/tmp/mcstatus.txt" replacing yes
			end tell
			--delay 1
			set filedata to readFile("/tmp/mcstatus.txt")
			
			set linedata to every paragraph of filedata
			set imageurl to item 1 of linedata
			
			tell application "URL Access Scripting"
				download imageurl to "/tmp/imagedata" replacing yes
			end tell
			
			tell application "Image Events"
				launch
				set imagedata to open "/tmp/imagedata"
				tell imagedata
					save in ("/tmp/imagedata.tiff") as TIFF
				end tell
			end tell
			
			set imgfd to open for access "/tmp/imagedata.tiff"
			set tiffdata to (read imgfd as "TIFF")
			
			set my_info to {item 2 of linedata, item 3 of linedata, item 4 of linedata, item 5 of linedata, tiffdata}
			return my_info
		on error
			set my_info to {"", "", "", 0, missing value}
			return my_info
		end try
	end if
end tell

on readFile(unixPath)
	set theFileReference to open for access unixPath
	set theFileContents to read theFileReference
	close access theFileReference
	return theFileContents
end readFile
