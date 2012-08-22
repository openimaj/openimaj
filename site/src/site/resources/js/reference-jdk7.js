var ids = new Array();

//remove links
$('a[href*="Reference.html"], a[href*="References.html"], a[href*="ReferenceType.html"]').each(function (it) {
  html = $(this).html();
  $(this).before(html);
  $(this).remove();
});

//process all @References first:
var allRefs = getReferences();
for (var i=0; i<allRefs.length; i++) {
	var refs = allRefs[i];
	
	addReferences(refs);
}

//then do any remaining @Reference annotations:
var allRef = getReference();
for (var i=0; i<allRef.length; i++) {
	var ref = allRef[i];
	
	addReference(ref);
}

for (var i=0; i<ids.length; i++) {
	var id = ids[i];
	
	$('#' + id + '-references div').hide();
	$('#' + id + '-references div:first').show();
	$('#' + id + '-references ul li:first').addClass('active');
	
	var cb = function(id) {
		return function() {
	    $('#' + id + '-references ul li').removeClass('active');
	    $(this).parent().addClass('active');
	    var currentTab = $(this).attr('href');
	    $('#' + id + '-references div').hide();
	    $(currentTab).show();
	    return false;
	}};
	
	$('#' + id + '-references ul li a').click(cb(id));
}


/* FUNCTIONS BELOW HERE */

function getRefId(ref) {
	if (ref.parentNode.parentNode.previousSibling != null && ref.parentNode.parentNode.previousSibling.previousSibling != null && ref.previousSibling.previousSibling.nodeName.toLowerCase() == "h4") {
		var id = ref.parentNode.parentNode.previousSibling.previousSibling.name;
		id = escape(id).replace(/%/g, "").replace(/\./g, "-");
		return id;
	}
	return "master";
}

function getInsertionPoint(ref, id) {
	var hr = null;
	
	if (id != "master") {
		//method node
		hr = ref.parentNode;
	} else {
		//master node
		hr = ref.parentNode.parentNode.parentNode;
	}
	return hr;
}

function addContainer(ref, id) {
	if (document.getElementById(id + "-references") == null) {
		var container = buildContainer(id);
		var ip = getInsertionPoint(ref, id);
		//ip.parentNode.insertBefore(container, ip);
		ip.appendChild(container);
		ids.push(id);
	}
}

function addReferences(refs) {
	var id = getRefId(refs);
	addContainer(refs, id);
	
	var references = parseReferences(refs.innerHTML.substring(0, refs.innerHTML.indexOf(")\n") + 1));
	for (var j=0; j<references.length; j++) {
		var reference = references[j];

		addTextRef(id, reference);
		addBibTexRef(id, reference);
	}
	addAnnotationRef(id, refs);
	
	//refs.parentNode.removeChild(refs);
	refs.innerHTML = refs.innerHTML.substring(refs.innerHTML.indexOf(")\n") + 2);
}

function addReference(ref) {
	var id = getRefId(ref);
	addContainer(ref, id);

	var reference = parseReference(ref.innerHTML.substring(0, ref.innerHTML.indexOf(")\n") + 1));
	addTextRef(id, reference);
	addBibTexRef(id, reference);
	addAnnotationRef(id, ref);
	
	//ref.parentNode.removeChild(ref);
	ref.innerHTML = ref.innerHTML.substring(ref.innerHTML.indexOf(")\n") + 2);
}

function addAnnotationRef(id, ref) {
	var annotation = document.createElement("pre");
	annotation.class = "processed";
	annotation.style.fontSize = "small";
 	annotation.innerHTML = ref.innerHTML.substring(0, ref.innerHTML.indexOf(")\n") + 1);
 	document.getElementById(id + "-annotationRefs").appendChild(annotation);
}

function addTextRef(id, reference) {
	var html = document.createElement("span");
  html.className = "textReference";
  html.innerHTML = referenceToHTML(reference);
 	document.getElementById(id + "-plainTextRefs").appendChild(html);
}

function addBibTexRef(id, reference) {
	var bibtex = document.createElement("pre");
 	bibtex.style.fontSize = "small";
 	bibtex.innerHTML = referenceToBibtex(reference);
 	document.getElementById(id + "-bibtexRefs").appendChild(bibtex);
}

function buildContainer(id) {
    var outer = document.createElement("div");
    outer.className = "references";
		outer.id = id + "-references";

    var frag = "";
    frag += "<b>Bibliography:</b>";
    frag += "<ul>";
    frag += "<li><a href='#" + id + "-tab-1'>Plain Text</a></li>";
    frag += "<li><a href='#" + id + "-tab-2'>BibTex</a></li>";
    frag += "<li><a href='#" + id + "-tab-3'>OpenIMAJ Annotation</a></li>";
    frag += "</ul>";
    frag += "<div id='" + id + "-tab-1'>";
    frag += "<span id='" + id + "-plainTextRefs'></span>";
    frag += "</div>";
    frag += "<div id='" + id + "-tab-2'>";
    frag += "<span id='" + id + "-bibtexRefs'></span>";
    frag += "</div>";
    frag += "<div id='" + id + "-tab-3'>";
    frag += "<span id='" + id + "-annotationRefs'></span>";
    frag += "</div>";

    outer.innerHTML = frag;

    return outer;
}

function getReference() {
    var potential = document.getElementsByTagName("pre");

		var allRef = new Array();
    for (var i = 0; i < potential.length; i++) {
        if (potential[i].class != "processed" && potential[i].innerHTML.indexOf("@Reference") != -1) {
            allRef.push(potential[i]);
        }
    }

    return allRef;
}

function getReferences() {
    var potential = document.getElementsByTagName("pre");
		
		var allRefs = new Array();
    for (var i = 0; i < potential.length; i++) {
        if (potential[i].class != "processed" && potential[i].innerHTML.indexOf("@References") != -1) {
            allRefs.push(potential[i]);
        }
    }

    return allRefs;
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
    refString = refString.replace(/\\'/g, "'"); //change \ to '
    refString = refString.replace(/\\/g, "\\\\\\"); //change \ to \\
    refString = "{" + refString + "}";

    var obj = jQuery.parseJSON(refString);

		//check authors & editors are arrays
		if (obj.author != null && !(obj.author instanceof Array)) obj.author = new Array(obj.author);
		if (obj.editor != null && !(obj.editor instanceof Array)) obj.editor = new Array(obj.editor);

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
			return formatName(authors[0]) + ". "
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
    builder += (ref.title + (ref.title[ref.title.length-1] == "." ? " " : ". "));
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

		builder = normalise(builder);
    return builder;
}

function normalise(value) {
    value = value.replace(/\\glqq\s?/g, "&bdquo;");
    value = value.replace(/\\grqq\s?/g, '&rdquo;');
    value = value.replace(/\\ /g, '&nbsp;');
    value = value.replace(/\\url/g, '');
    value = value.replace(/---/g, '&mdash;');
    value = value.replace(/\\"a/g, '&auml;');
    value = value.replace(/\\"o/g, '&ouml;');
    value = value.replace(/\\"u/g, '&uuml;');
    value = value.replace(/\\"A/g, '&Auml;');
    value = value.replace(/\\"O/g, '&Ouml;');
    value = value.replace(/\\"U/g, '&Uuml;');
    value = value.replace(/\\ss/g, '&szlig;');
    return value;
  }

function referenceToBibtex(ref) {
    return formatReferenceBibtex(ref, makeKey(ref));
}

function referenceToHTML(ref) {
		return formatReferenceHTML(ref);
}

