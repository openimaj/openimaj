var ref = hasReference();
if (ref != null) {
    var pane = buildContainer("master");
    
		if (ref.previousSibling != null && ref.previousSibling.previousSibling.nodeName.toLowerCase() == "h3") {
			//this is an annotation on a method
			var hr = ref;
  		while ((hr = hr.nextSibling) != null) {
			  if (hr.nodeName.toLowerCase() == "hr") break;
		  }
			hr.parentNode.insertBefore(pane, hr);
		} else {
			//class annotation
	  	var hr = ref.parentNode.parentNode;
  		while ((hr = hr.nextSibling) != null) {
			  if (hr.nodeName.toLowerCase() == "hr") break;
		  }
		
		  ref.parentNode.parentNode.parentNode.insertBefore(pane, hr);
	  }

		if (hasReferences() != null) {
			var potential = document.getElementsByTagName("font");
	    for (var i = 0; i < potential.length; i++) {
	        if (potential[i].innerHTML.indexOf("@References") != -1) {
	            var references = parseReferences(potential[i].innerHTML);

							for (var j=0; j<references.length; j++) {
								var reference = references[j];
								
								var html = document.createElement("span");
	            	html.className = "textReference";
	            	html.innerHTML = referenceToHTML(reference);
	            	document.getElementById("master-plainTextRefs").appendChild(html);

	            	var bibtex = document.createElement("pre");
	            	bibtex.style.fontSize = "small";
	            	bibtex.innerHTML = referenceToBibtex(reference);
	            	document.getElementById("master-bibtexRefs").appendChild(bibtex);
						}
						
						var annotation = document.createElement("pre");
          	annotation.appendChild(potential[i]);
          	document.getElementById("master-annotationRefs").appendChild(annotation);
	        }
	    }
		} else {
		  var potential = document.getElementsByTagName("font");
	    for (var i = 0; i < potential.length; i++) {
	        if (potential[i].innerHTML.indexOf("@Reference") != -1) {
	            var reference = parseReference(potential[i].innerHTML);

							var html = document.createElement("span");
	            html.className = "textReference";
	            html.innerHTML = referenceToHTML(reference);
	            document.getElementById("master-plainTextRefs").appendChild(html);

	            var bibtex = document.createElement("pre");
	            bibtex.style.fontSize = "small";
	            bibtex.innerHTML = referenceToBibtex(reference);
	            document.getElementById("master-bibtexRefs").appendChild(bibtex);

	            var annotation = document.createElement("pre");
	            annotation.appendChild(potential[i]);
	            document.getElementById("master-annotationRefs").appendChild(annotation);
	        }
	    }	
		}
}

$('.references div').hide();
$('.references div:first').show();
$('.references ul li:first').addClass('active');

$('.references ul li a').click(function() {
    $('.references ul li').removeClass('active');
    $(this).parent().addClass('active');
    var currentTab = $(this).attr('href');
    $('.references div').hide();
    $(currentTab).show();
    return false;
});


/* FUNCTIONS BELOW HERE */

function buildContainer(id) {
    var outer = document.createElement("div");
    outer.className = "references";
		outer.id = id + "-references";

    var frag = "";
    frag += "<b>Bibliography:</b>";
    frag += "<ul>";
    frag += "<li><a href='#tab-1'>Plain Text</a></li>";
    frag += "<li><a href='#tab-2'>BibTex</a></li>";
    frag += "<li><a href='#tab-3'>OpenIMAJ Annotation</a></li>";
    frag += "</ul>";
    frag += "<div id='tab-1'>";
    frag += "<span id='" + id + "-plainTextRefs'></span>";
    frag += "</div>";
    frag += "<div id='tab-2'>";
    frag += "<span id='" + id + "-bibtexRefs'></span>";
    frag += "</div>";
    frag += "<div id='tab-3'>";
    frag += "<span id='" + id + "-annotationRefs'></span>";
    frag += "</div>";

    outer.innerHTML = frag;

    return outer;
}

function hasReference() {
    var potential = document.getElementsByTagName("font");

    for (var i = 0; i < potential.length; i++) {
        if (potential[i].innerHTML.indexOf("@Reference") != -1) {
            return potential[i].parentNode;
        }
    }

    return null;
}

function hasReferences() {
    var potential = document.getElementsByTagName("font");

    for (var i = 0; i < potential.length; i++) {
        if (potential[i].innerHTML.indexOf("@References") != -1) {
            return potential[i].parentNode;
        }
    }

    return null;
}

function parseReferences(refString) {
	refString = refString.replace(/^\s+|\s+$/gm, "");
	refString = refString.replace(/\s+/gm, " ");
	refString = refString.replace("@References(references={", "");
	refString = refString.replace(/}\)$/, "");
	refString = refString.replace(/,([^,]*?)=/g, ",\n$1=");
	
	var parts = refString.split("@Reference");
	
	var references = new Array();
	for (var i=0; i<parts.length; i++) {
		parts[i] = parts[i].replace(/^\s+|\s+$/gm, "");

		if (parts[i].length > 0) {
			references.push(parseReference("@Reference"+parts[i]));
		}
	}
	return references;
}

//Convert a single reference object to a Javascript object
function parseReference(refString) {
		refString = refString.replace(/^\s+|\s+$/gm, ""); //remove whitespace
    refString = refString.replace("@Reference(", ""); //remove @Reference(
    refString = refString.replace(/^(.+?)=/gm, "\"$1\":"); //quote all keys
    refString = refString.replace(/\),?$/g, ""); //remove last )
    refString = refString.replace(/=/g, ":"); //change = to :
    refString = refString.replace(/{/g, "["); //change { to [
    refString = refString.replace(/}/g, "]"); //change } to }
    refString = refString.replace(/"type":(.*),/g, "\"type\":\"$1\","); //quote type
    refString = refString.replace(/\\'/g, "'"); //change \' to '
    refString = "{" + refString + "}";

    var obj = jQuery.parseJSON(refString);

    //convert customData
    if (obj.customData != null) {
        var cd = new Object();

        for (var i = 0; i < obj.customData.length; i += 2) {
            cd[obj.customData[i]] = obj.customData[i + 1];
        }

        obj.customData = cd;
    }

    return obj;
}

function appendNamesBibtex(key, authors) {
    if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length == 0))
    return "";

    var builder = " " + key + " = {";
    for (var i = 0; i < authors.length - 1; i++) {
        builder += "{" + authors[i] + "} and ";
    }
    builder += "{" + authors[authors.length - 1] + "}";
    builder += "}\n";

    return builder;
}

function formatReferenceBibtex(ref, key) {
    var builder = "";

    builder += ("@" + ref.type.toLowerCase() + "{" + key + "\n");
    builder += appendNamesBibtex("author", ref.author);

    builder += (" title = {" + ref.title + "}\n");
    builder += (" year = {" + ref.year + "}\n");

    if (ref.journal != null && ref.journal.length > 0) builder += (" journal = {" + ref.journal + "}\n");
    if (ref.booktitle != null && ref.booktitle.length > 0) builder += (" booktitle = {" + ref.booktitle + "}\n");
    if (ref.pages != null && ref.pages.length > 0) {
        if (ref.pages.length == 1) builder += (" pages = {" + ref.pages[0] + "}\n");
        else if (ref.pages.length == 2) builder += (" pages = {" + ref.pages[0] + "--" + ref.pages[1] + "}\n");
        else {
            builder += (" pages = {");
            for (var i = 0; i < ref.pages.length - 1; i++) builder += (ref.pages[i] + ", ");
            builder += (ref.pages[ref.pages.length - 1] + "}\n");
        }
    }

    if (ref.chapter != null && ref.chapter.length > 0) builder += (" chapter = {" + ref.chapter + "}\n");
    if (ref.edition != null && ref.edition.length > 0) builder += (" edition = {" + ref.edition + "}\n");
    if (ref.url != null && ref.url.length > 0) builder += (" url = {" + ref.url + "}\n");
    if (ref.note != null && ref.note.length > 0) builder += (" note = {" + ref.note + "}\n");

    builder += appendNamesBibtex("editor", ref.editor);

    if (ref.institution != null && ref.institution.length > 0) builder += (" institution = {" + ref.institution + "}\n");
    if (ref.month != null && ref.month.length > 0) builder += (" month = {" + ref.month + "}\n");
    if (ref.number != null && ref.number != "") builder += (" number = {" + ref.number + "}\n");
    if (ref.organization != null && ref.organization.length > 0) builder += (" organization = {" + ref.organization + "}\n");
    if (ref.publisher != null && ref.publisher.length > 0) builder += (" publisher = {" + ref.publisher + "}\n");
    if (ref.school != null && ref.school.length > 0) builder += (" school = {" + ref.school + "}\n");
    if (ref.series != null && ref.series.length > 0) builder += (" series = {" + ref.series + "}\n");
    if (ref.volume != null && ref.volume != "") builder += (" volume = {" + ref.volume + "}\n");

    if (ref.customData != null && ref.customData.length > 1) {
        for (var i = 0; i < ref.customData.length; i += 2) {
            builder += (" " + ref.customData[i] + " = {" + ref.customData[i + 1] + "}\n");
        }
    }

    builder += ("}\n");

    return builder;
}

function makeKey(ref) {
    var authors = ref.author;

    if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length == 0))
    return "OpenIMAJ-1";

    var lastName = "";
    if (authors[0].indexOf(",") == -1) {
        lastName = authors[0].substring(authors[0].lastIndexOf(" "));
    } else {
        lastName = authors[0].substring(0, authors[0].indexOf(","));
    }

    return lastName + ref.year;
}

function appendNames(authors) {
    if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length == 0))
    return "";

		if (authors.length == 1) {
			return formatName(authors[0]) + "."
		}

		var builder = "";
    for (var i = 0; i < authors.length - 2; i++) {
        builder += (formatName(authors[i]) + ", ");
    }
    builder += (formatName(authors[authors.length - 2]) + " and " + formatName(authors[authors.length - 1]) + ". ");

		return builder;
}

function formatName(name) {
    if (name.indexOf(",") != -1) {
        var lastName = name.substring(0, name.indexOf(","));
        var firstNames = name.substring(name.indexOf(",") + 1).split(" ");

        var formatted = "";
        for (var i=0; i<firstNames.length; i++) {
            firstNames[i] = firstNames[i].replace(/^\s+|\s+$/gm, "");;
            if (firstNames[i].length > 0)
            formatted += firstNames[i].charAt(0) + ". ";
        }

        return formatted + lastName;
    } else {
        var parts = name.split(" ");
        var formatted = "";

        for (var i = 0; i < parts.length - 1; i++) {
            formatted += parts[i].replace(/^\s+|\s+$/gm, "").charAt(0) + ". ";
        }

        return formatted + parts[parts.length - 1];
    }
}

function formatReferenceHTML(ref) {
    var builder = "";

    builder += ("<span class='authors'>");
    builder += appendNames(ref.author);
    builder += ("</span>");

    builder += ("<span class='title'>");
    builder += (ref.title + ". ");
    builder += ("</span>");

    if (ref.journal != null && ref.journal.length > 0) builder += ("<span class='journal'>" + ref.journal + ". </span>");
    if (ref.booktitle != null && ref.booktitle.length > 0) builder += ("<span class='booktitle'>" + ref.booktitle + ". </span>");
    if (ref.institution != null && ref.institution.length > 0) builder += ("<span class='institution'>" + ref.institution + ". </span>");
    if (ref.school != null && ref.school.length > 0) builder += ("<span class='school'>" + ref.school + ". </span>");
    if (ref.publisher != null && ref.publisher.length > 0) builder += ("<span class='publisher'>" + ref.publisher + ". </span>");
    if (ref.organization != null && ref.organization.length > 0) builder += ("<span class='organization'>" + ref.organization + ". </span>");

    if (ref.pages != null && ref.pages.length > 0) {
        if (ref.pages.length == 1) builder += ("<span class='pages'>" + "p" + ref.pages[0] + ". </span>");
        else if (ref.pages.length == 2) builder += ("<span class='pages'>" + "pp" + ref.pages[0] + "-" + ref.pages[1] + ". </span>");
        else {
            builder += ("<span class='pages'>" + "pp");
            for (var i = 0; i < ref.pages.length - 1; i++) builder += (ref.pages[i] + ", ");
            builder += (ref.pages[ref.pages.length - 1] + ". </span>");
        }
    }

    if (ref.month != null && ref.month.length > 0) builder += ("<span class='month'>" + ref.month + ", </span>");
    builder += ("<span class='year'>" + ref.year + ". </span>");

    if (ref.url != null && ref.url.length > 0) builder += ("<a class='url' href='" + ref.url + "'>" + ref.url + "</a>");

    return builder;
}

function referenceToBibtex(ref) {
    return formatReferenceBibtex(ref, makeKey(ref));
}

function referenceToHTML(ref) {
		return formatReferenceHTML(ref);
}

