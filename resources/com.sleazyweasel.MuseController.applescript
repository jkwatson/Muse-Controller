tell application id "com.sleazyweasel.MuseController"
	if it is running then
		try
			do shell script "curl -L http://localhost:23233/pandora/airfoilstatusdata -o /tmp/mcstatus.txt"
			set theFileReference to open for access "/tmp/mcstatus.txt"
			set filedata to read theFileReference
			close access theFileReference
			
			set linedata to every paragraph of filedata
			set imageurl to item 1 of linedata
			--			do shell script "logger System " & imageurl
			do shell script "curl -L " & imageurl & " -o /tmp/imagedata.jpg"
			
			
			tell application "Image Events"
				launch
				set imagedata to open "/tmp/imagedata.jpg"
				tell imagedata
					scale to size 512
					save in ("/tmp/imagedata.tiff") as TIFF
				end tell
				close imagedata
				quit
			end tell
			
			try
				--				do shell script "logger System before opening tiff"
				set tiffdata to (read "/tmp/imagedata.tiff" as "TIFF")
				--				do shell script "logger System after reading tiff"
			on error errStr number errorNumber
				do shell script "logger System Airfoil/Muse Controller Error: " & errStr
				set tiffdata to missing value
			end try
			
			set my_info to {item 2 of linedata, item 3 of linedata, item 4 of linedata, item 5 of linedata, tiffdata}
			return my_info
		on error errStr number errorNumber
			do shell script "logger System Airfoil/Muse Controller Error: " & errStr
			set my_info to {"", "", "", 0, missing value}
			return my_info
		end try
	end if
end tell

