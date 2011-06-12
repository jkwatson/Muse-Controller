#!/usr/bin/perl

# Copyright (c) 2011
# Juan C. Muller <jcmuller@gmail.com>

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

use strict;
use warnings;

# Use this script to be able to use change-station-dmenu.sh as
# event_command = $HOME/.config/pianobar/dmenu.pl

# (taken from https://github.com/jcmuller/pianobar-notify)
	my $command = shift;
	open( my $debugHandle, ">>/tmp/pianobar_debug.txt") or die "Couldn't open debug file for writing: $!";
	print $debugHandle "$command\n";
		my $data = '/tmp/pianobar_data';
		open(my $fh, ">$data") or die "Couldn't open $data for writing: $!";
		while (<STDIN>)
		{
			#print $fh "$1. $2\n" if (/station(\d+)=(.+)$/);
			print $fh "$_";
			print $debugHandle "$_";
		}

		close($fh);
	print $debugHandle "\n";
	close($debugHandle);
