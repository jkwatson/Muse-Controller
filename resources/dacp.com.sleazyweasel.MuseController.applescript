on remote_stop()
end remote_stop

on remote_play()
	tell application "URL Access Scripting"
		download "http://localhost:23233/pianobar/playpause" to "/tmp/mcstatus.json" replacing yes
	end tell
end remote_play

on remote_pause()
	tell application "URL Access Scripting"
		download "http://localhost:23233/pianobar/playpause" to "/tmp/mcstatus.json" replacing yes
	end tell
end remote_pause

on remote_next_item()
	tell application "URL Access Scripting"
		download "http://localhost:23233/pianobar/next" to "/tmp/mcstatus.json" replacing yes
	end tell
end remote_next_item

on remote_previous_item()
end remote_previous_item

on remote_begin_seek_backward()
end remote_begin_seek_backward

on remote_begin_seek_forward()
end remote_begin_seek_forward

on remote_end_seek()
end remote_end_seek

on remote_volume_up()
	tell application "URL Access Scripting"
		download "http://localhost:23233/pianobar/volumeUp" to "/tmp/mcstatus.json" replacing yes
	end tell
end remote_volume_up

on remote_volume_down()
	tell application "URL Access Scripting"
		download "http://localhost:23233/pianobar/volumeDown" to "/tmp/mcstatus.json" replacing yes
	end tell
end remote_volume_down

on remote_mute()
end remote_mute

on remote_shuffle()
end remote_shuffle

on remote_restart_item()
end remote_restart_item

on remote_other()
end remote_other