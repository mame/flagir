#!/usr/bin/env ruby19

hash = {}
File.foreach(ARGV[0]) do |line|
  title, svg = line.chomp.split(":")
  hash[File.basename(svg, ".svg")] = title
end

File.foreach(ARGV[1]) do |line|
  png, ary, time = line.chomp.split(":")
  basename = File.basename(png, ".png")
  title = hash[basename]
  print [basename.size].pack("c") + basename
  print [title.size].pack("c") + title
  print ary.split(",").map {|n| n.to_i }.pack("v*")
end
