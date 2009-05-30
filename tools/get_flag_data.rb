#!/usr/bin/env ruby19
# coding: UTF-8

wikipedia_entries = %w(
  Gallery_of_sovereign-state_flags
  Flags_of_unrecognized_and_partially_recognized_states
)

require "open-uri"

countries = {}
flag = false
wikipedia_entries.each do |entry|
  uri = "http://en.wikipedia.org/w/index.php?title=#{ entry }&action=edit"
  URI.parse(uri).read.each_line do |line|
    case line
    when /^\{\{Begin flag gallery\}\}/ then flag = true
    when /^\{\{End flag gallery\}\}/   then flag = false
    when /^\{\{Flag entry\|Width=\d+\|(?<param>.+?)\}\}/
      next unless flag
      case $~[:param]
      when /^Country=(?<name>[^|]+)/
        countries[$~[:name]] = "Flag of #{ $~[:name] }.svg"
      when /^Image=(?<image>.+?)\|Caption=.*?\[\[(?<name>[^|\]]+)(?:\|(?!Flag)|\]\]$)/
        countries[$~[:name]] = $~[:image]
      else
        raise line
      end
    end
  end
end

countries.each do |country, path|
  raise country if country[/:/] || path[/:/]
  puts "#{ country }:#{ path }"

  uri = "http://commons.wikimedia.org/w/File:#{ URI.escape(path) }"
  %r(<a\s[^>]*href="(?<uri>http://upload.wikimedia.org/wikipedia/commons/\h+/\h+/(Flag_of_[^"]+|[^"]+_Flag)\.svg)"[^>]*>) =~
    URI.parse("http://en.wikipedia.org/wiki/File:#{ URI.escape(path) }").read.
    chars.map {|c| c.valid_encoding? ? c : "\ufffd" }.join
  svg = File.join("svg", path)
  png = File.join("png", File.basename(path, ".svg") + ".png")
  thumb = File.join("thumb", File.basename(path, ".svg") + ".png")
  open(svg, "wb") {|f| f.write URI.parse(uri).read }
  system("inkscape", "-z", "-f", svg, "-e", png, out: :close, err: :close)
  system("inkscape", "-z", "-f", svg, "-h", "60", "-e", thumb, out: :close, err: :close)
end
