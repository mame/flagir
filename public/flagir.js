$(function() {
    var applet = $("<applet/>")
    applet.attr("code", "Main")
    applet.attr("archive", "flagir-mini.jar")
    applet.attr("width", "376")
    applet.attr("height", "248")
    applet.attr("mayscript", "true")
    applet.append("")
    $("div#applet").append(applet)
})

ranking = null
notify_start = function() {
    ranking = $("<table/>")
}
notify_entry = function(score, path, title) {
    var odd = ranking.find("tr").length % 2 == 0 ? "odd" : "even"
    var uri = "http://en.wikipedia.org/wiki/" + encodeURI(title)
    var img = $("<img/>").attr("src", "thumb/" + encodeURI(path) + ".png")
    if (title == "Nepal") img.addClass("nepal")

    ranking.append($("<tr/>")
	.append(
	    $("<td/>").addClass(odd).addClass("flag")
		.append(
		    $("<a/>").attr("href", uri).append(img)))
	.append(
	    $("<td/>").addClass(odd).addClass("title")
		.append(
		    $("<span/>").addClass("score")
			.append("(score: " + score.toFixed(2) + ")"))
		.append("<br/>")
		.append(
		    $("<a/>").attr("href", uri).append(title))))
}
notify_end = function() {
    var div = $("#ranking")
    div.empty()
    div.append(ranking)
}
