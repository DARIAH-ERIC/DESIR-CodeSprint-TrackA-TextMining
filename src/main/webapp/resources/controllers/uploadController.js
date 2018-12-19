angular.module('org.dariah.desir.ui').controller('uploadController', function ($scope, $http) {
    var measurementMap = new Array();
    $scope.jsonResponse = null;
    $scope.processingAuthors = 'Authors';
    $scope.processingCitations = 'Citations';
    $scope.processingEntities = 'Named entities';

    var entity_fishing_host = "http://nerd.huma-num.fr/nerd/service";
    var pdfViewer = $('#pdf-viewer');
    //var canvas = $('#the-canvas');
    var scale_x ;
    var scale_y ;


    $scope.clear = function () {
        $scope.myFile = null;
        $scope.processingAuthors = 'Authors';
        $scope.processingCitations = 'Citations';
        $scope.processingEntities = 'Named entities';
        angular.element("div[id='pdf-viewer']").empty();
        angular.element("input[type='file']").val(null);
        $scope.jsonResponse = null;
        $scope.errorMessage = null;
        $scope.requestError = null;
    };

    $scope.uploadFile = function (type) {
        var urlPath = window.location.href;
        console.log(urlPath)
        $scope.jsonResponse = "";
        $scope.quantity = 4;

        var formData = new FormData();
        $scope.responseJson = "";
        formData.append("file", $scope.myFile);

        switch (type){
            case 'authors':
                urlPath = urlPath + 'processAuthor';
                $scope.processingAuthors = 'Processing...'
            break;
            case 'citations':
                urlPath = urlPath + 'processCitation';
                $scope.processingCitations = 'Processing...';
            break;
            case 'entities':
                urlPath ='/processNamedEntities'; // call to build entity fishing query
                $scope.processingEntities = 'Processing...';
            break;
        }


        var request = {
            method: 'POST',
            url: urlPath,
            data: formData,
            headers: {
                'Content-Type': undefined
            }
        };

        // SEND THE FILES.
        $http(request)
            .then(
                function (d) {  //success
                    $scope.jsonResponse = d.data;
                    setupAnnotations($scope.jsonResponse, type)
                    $scope.updateButton(type);
                },
                function (e) {  //error
                    $scope.jsonResponse = undefined;
                    $scope.requestError = e;
                }
            );
    }

    $scope.updateButton = function(type){
        switch (type){
            case 'authors':
                $scope.processingAuthors = 'Authors';
                $scope.processed = "Authors processed !!!"
                break;
            case 'citations':
                $scope.processingCitations = 'Citations';
                $scope.processed = "Citations processed !!!"
                break;
            case 'entities':
                $scope.processingEntities = 'Named entities';
                $scope.processed = "Named entities processed !!!"
                break;
        }
    }

    /*

     This is for entities fishing

     function setupAnnotations(response) {

         var json = response ;

         var pageInfo = response.pages;
         console.log(pageInfo)
         var page_height = 0.0;
         var page_width = 0.0;
         var entities = response.entities;
         console.log(entities[0])
         if (entities) {
             // hey bro, this must be asynchronous to avoid blocking the brother ;)
             entities.forEach(function (author, n) {
                 var entityDomain = author.domains;
                 measurementMap[n] = author;
                 //var theId = author.type;
                 var theUrl = null;
                 //var theUrl = annotation.url;
                 var pos = author.pos;
                 if ((pos != null) && (pos.length > 0)) {
                     pos.forEach(function (thePos, m) {
                         // get page information for the annotation
                         var pageNumber = thePos.p;
                         if (pageInfo[pageNumber - 1]) {
                             page_height = pageInfo[pageNumber - 1].page_height;
                             page_width = pageInfo[pageNumber - 1].page_width;
                         }
                         annotateEntity(thePos, theUrl, page_height, page_width, n, m);
                     });
                 }
             });
         }
     }


      function annotateEntity(thePos, theUrl, page_height, page_width, measurementIndex, positionIndex) {
         var page = thePos.p;
         var pageDiv = $('#the-canvas');
         pageDiv.innerHTML = ""
         var canvas = $('#the-canvas');
         //var canvas = pageDiv.find('canvas').eq(0);;

         var canvasHeight = canvas.height();
         var canvasWidth = canvas.width();
         var scale_x = canvasHeight / page_height;
         var scale_y = canvasWidth / page_width;
         console.log(scale_x)
         console.log(scale_y)


         var x = thePos.x * scale_x //- 1;
         var y = thePos.y * scale_y //- 1;

         console.log(x)
         console.log(y)

         var width = thePos.w * scale_x //+ 1;
         var height = thePos.h * scale_y //+ 1;

         //make clickable the area
         theId = "value";
      //  if (theId)
         //    theId = theId.replace(" ", "_");
         var element = document.createElement("a");
         var attributes = "margin-top : 205px; margin-left: 155px; display:block; width:" + width + "px; height:" + height + "px; position:absolute; top:" +
             y + "px; left:" + x + "px;";
         element.setAttribute("style", attributes + "border:2px solid; border-color: red");
         //element.setAttribute("style", attributes + "border:2px solid;");
         element.setAttribute("class", theId);
         element.setAttribute("id", 'annot-' + measurementIndex + '-' + positionIndex);
         element.setAttribute("page", page);

         $('#pdf').append(element);
         $('#pdf').on("hover", '#annot-' + measurementIndex + '-' + positionIndex, viewQuantityPDF);
         $('#pdf').on("click", '#annot-' + measurementIndex + '-' + positionIndex, viewQuantityPDF);

         pageDiv.height(canvasHeight);
         pageDiv.width(canvasWidth);
     }*/

// for associating several entities to an annotation position (to support nbest mode visualisation)
    var entityMap = new Object();
// for complete concept information, resulting of additional calls to the knowledge base service
    var conceptMap = new Object();
    function setupAnnotations(response, type) {

        var json = response ;
        var page_height;
        var page_width;
        var result;

        $scope.errorMessage = null;

        if(type != "entities") {
            page_height = response.pageDimention.height;
            page_width = response.pageDimention.width;
            result = response.results;
        }

        if (type === 'authors') {
            var process = true;
            var count = 0;
            // hey bro, this must be asynchronous to avoid blocking the brother ;)
            result.forEach(function (author, n) {
                count++;
                if(author['coordinates'] === null){
                    process = process && false;
                    console.log(process)
                }
                else{
                    process = process && true;
                    var coordinates = author.coordinates;
                    if(coordinates.indexOf(';') !== -1){
                        coordinates = coordinates.split(";")[1].split(",");
                    }
                    else{
                        coordinates = coordinates.split(",");
                    }
                    annotateAuthors(coordinates, author, page_width,  page_height)
                }
                if(count === result.length ) {
                    if(process === false){
                        $scope.errorMessage = 'Something went wrong server side for some authors!!!';
                        $scope.jsonResponse = null;
                    }
                    $(document).ready(function(){
                        $('[data-toggle="tooltip"]').tooltip();
                    });
                }
            });
        }

        if ( type === 'citations' ) {
            result.forEach(function (citation, n) {
                var coordinates = citation.coordinates;
                var coords = []; var coordinatesTem=[]; var page = 0;
                if(coordinates.indexOf(';') !== -1){
                    coordinates = coordinates.split(";")
                    coordinates_0 = coordinates;
                    page =  coordinates_0[0];

                    for(var j = 0; j < coordinates.length; j++) {
                        coordinatesTem = coordinates[j].split(',');
                        if(j === 0){
                            coords[0] = coordinatesTem[1];
                            coords[1] = coordinatesTem[2];
                            coords[2] = coordinatesTem[3];
                            coords[3] = parseFloat(coordinatesTem[4]);
                        }
                        else{
                            if(parseInt(coords[2]) < parseInt(coordinatesTem[3])) {
                                coords[2] = coordinatesTem[3];
                            }


                            coords[3] += parseFloat(coordinatesTem[4]);
                        }
                    }
                }
                else{
                    coordinates = coordinates.split(",");
                    console.log(coordinates)
                }
                annotateCitations(page, coords, citation.wikidataID, citation.doi , page_width,  page_height)
            });
        } if(type == "entities"){
            if(count === response.length ) {
                if(process === false){
                    $scope.errorMessage = 'Error encountered while receiving the server\'s answer: response is empty.!!!';
                    $scope.jsonResponse = null;
                    return;
                }
                $(document).ready(function(){
                    $('[data-toggle="tooltip"]').tooltip();
                });
            }
            console.log(response)
            responseJson = response;
            var pageInfo = response.pages;
            var page_height = 0.0;
            var page_width = 0.0;

            var entities = response.entities;
            if (entities) {
                // hey bro, this must be asynchronous to avoid blocking the brothers
                entities.forEach(function (entity, n) {
                    var entityType = entity.type;
                    if (!entityType) {
                        if (entity.domains && entity.domains.length > 0)
                            entityType = entity.domains[0]
                    }

                    entityMap[n] = [];
                    entityMap[n].push(entity);

                    var lang = 'en'; //default
                    var language = response.language;
                    if (language)
                        lang = language.lang;

                    var identifier = entity.wikipediaExternalRef;
                    if (identifier && (conceptMap[identifier] == null)) {
                        fetchConcept(identifier, lang, function (result) {
                            conceptMap[result.wikipediaExternalRef] = result;
                        });
                    }

                    //var theId = measurement.type;
                    //var theUrl = null;
                    //var theUrl = annotation.url;
                    var pos = entity.pos;
                    if ((pos != null) && (pos.length > 0)) {
                        pos.forEach(function (thePos, m) {
                            // get page information for the annotation
                            var pageNumber = thePos.p;
                            if (pageInfo[pageNumber - 1]) {
                                page_height = pageInfo[pageNumber - 1].page_height;
                                page_width = pageInfo[pageNumber - 1].page_width;
                            }
                            annotateEntity(entityType, thePos, page_height, page_width, n, m);
                        });
                    }
                });
                console.log(entityMap)
            }

        }
    }


    function fetchConcept(identifier, lang, successFunction) {
        $.ajax({
            type: 'GET',
            url: entity_fishing_host+'/kb/concept/' + identifier + '?lang=' + lang,
            success: successFunction,
            dataType: 'json'
        });
    }

    function annotateEntity(theType, thePos, page_height, page_width, entityIndex, positionIndex) {
        var page = thePos.p;
        var pageDiv = $('#page-' + page);
        var canvas = pageDiv.children('canvas').eq(0);
        //var canvas = pageDiv.find('canvas').eq(0);;

        var canvasHeight = canvas.height();
        var canvasWidth = canvas.width();
        var scale_x = canvasHeight / page_height;
        var scale_y = canvasWidth / page_width;

        var x = thePos.x * scale_x - 1;
        var y = thePos.y * scale_y - 1;
        var width = thePos.w * scale_x + 1;
        var height = thePos.h * scale_y + 1;

        //make clickable the area
        theType = "" + theType;
        if (theType)
            theType = theType.replace(" ", "_");
        var element = document.createElement("a");
        var attributes = "display:block; width:" + width + "px; height:" + height + "px; position:absolute; top:" +
            y + "px; left:" + x + "px;";
        element.setAttribute("style", attributes + "border-width: 2px;border-style:solid; "); //border-color: " + getColor(theId) +";");
        //element.setAttribute("style", attributes + "border:2px solid;");
        element.setAttribute("class", theType.toLowerCase());
        element.setAttribute("id", 'annot-' + entityIndex + '-' + positionIndex);
        element.setAttribute("page", page);

        pageDiv.append(element);

        $('#annot-' + entityIndex + '-' + positionIndex).bind('hover', viewEntityPDF);
        $('#annot-' + entityIndex + '-' + positionIndex).bind('click', viewEntityPDF);
    }

    function getDefinitions(identifier) {
        var localEntity = conceptMap[identifier];
        if (localEntity != null) {
            return localEntity.definitions;
        } else
            return null;
    }

    function getPreferredTerm(identifier) {
        var localEntity = conceptMap[identifier];
        if (localEntity != null) {
            return localEntity.preferredTerm;
        } else
            return null;
    }

    function viewEntityPDF() {
        var pageIndex = $(this).attr('page');
        var localID = $(this).attr('id');

        console.log('viewEntityPDF ' + pageIndex + ' / ' + localID);

        if (responseJson == null)
            return;

        if (responseJson.entities == null) {
            return;
        }

        var topPos = $(this).position().top;

        var ind1 = localID.indexOf('-');
        var localEntityNumber = parseInt(localID.substring(ind1 + 1, localID.length));

        if ((entityMap[localEntityNumber] == null) || (entityMap[localEntityNumber].length == 0)) {
            // this should never be the case
            console.log("Error for visualising annotation with id " + localEntityNumber
                + ", empty list of entities");
        }

        var lang = 'en'; //default
        var language = responseJson.language;
        if (language)
            lang = language.lang;
        var string = "";
        for (var entityListIndex = entityMap[localEntityNumber].length - 1;
             entityListIndex >= 0;
             entityListIndex--) {
            var entity = entityMap[localEntityNumber][entityListIndex];
            var wikipedia = entity.wikipediaExternalRef;
            var wikidataId = entity.wikidataId;
            var domains = entity.domains;
            var type = entity.type;

            var colorLabel = null;
            if (type)
                colorLabel = type;
            else if (domains && domains.length > 0) {
                colorLabel = domains[0].toLowerCase();
            }
            else
                colorLabel = entity.rawName;

            var subType = entity.subtype;
            //var conf = entity.nerd_score;
            var conf = entity.nerd_selection_score;
            //var definitions = entity.definitions;
            var definitions = getDefinitions(wikipedia);

            var content = entity.rawName;
            //var normalized = entity.preferredTerm;
            var normalized = getPreferredTerm(wikipedia);

            var sense = null;
            if (entity.sense)
                sense = entity.sense.fineSense;

            string += "<div class='info-sense-box " + colorLabel + "'";
            if (topPos != -1)
                string += " style='vertical-align:top; position:relative; top:" + topPos + "'";

            string += "><h3 style='color:#FFF;padding-left:10px;'>" + content.toUpperCase() +
                "</h3>";
            string += "<div class='container-fluid' style='background-color:#F9F9F9;color:#70695C;border:padding:5px;margin-top:5px;'>" +
                "<table style='width:100%;background-color:#fff;border:0px'><tr style='background-color:#fff;border:0px;'><td style='background-color:#fff;border:0px;'>";

            if (type)
                string += "<p>Type: <b>" + type + "</b></p>";

            if (sense) {
                // to do: cut the sense string to avoid a string too large
                if (sense.length <= 20)
                    string += "<p>Sense: <b>" + sense + "</b></p>";
                else {
                    var ind = sense.indexOf('_');
                    if (ind != -1) {
                        string += "<p>Sense: <b>" + sense.substring(0, ind + 1) + "<br/>" +
                            sense.substring(ind + 1, sense.length) + "</b></p>";
                    }
                    else
                        string += "<p>Sense: <b>" + sense + "</b></p>";
                }
            }
            if (normalized)
                string += "<p>Normalized: <b>" + normalized + "</b></p>";

            if (domains && domains.length > 0) {
                string += "<p>Domains: <b>";
                for (var i = 0; i < domains.length; i++) {
                    if (i != 0)
                        string += ", ";
                    string += domains[i];
                }
                string += "</b></p>";
            }

            string += "<p>conf: <i>" + conf + "</i></p>";
            string += "</td><td style='align:right;bgcolor:#fff'>";
            string += '<span id="img-' + wikipedia + '"><script type="text/javascript">lookupWikiMediaImage("' + wikipedia + '", "' + lang + '")</script></span>';

            string += "</td></tr></table>";

            // definition
            if ((definitions != null) && (definitions.length > 0)) {
                var localHtml = wiki2html(definitions[0]['definition'], lang);
                string += "<p><div class='wiky_preview_area2'>" + localHtml + "</div></p>";
            }
            //
            // // statements
            // var statements = getStatements(wikipedia);
            // if ((statements != null) && (statements.length > 0)) {
            //     var localHtml = "";
            //     for (var i in statements) {
            //         var statement = statements[i];
            //         localHtml += displayStatement(statement);
            //     }
            //     //string += "<p><div><table class='statements' style='width:100%;border-color:#fff;border:1px'>" + localHtml + "</table></div></p>";
            //
            //     // make the statements information collapsible
            //     string += "<p><div class='panel-group' id='accordionParent'>";
            //     string += "<div class='panel panel-default'>";
            //     string += "<div class='panel-heading' style='background-color:#F9F9F9;color:#70695C;border:padding:0px;font-size:small;'>";
            //     // accordion-toggle collapsed: put the chevron icon down when starting the page; accordion-toggle : put the chevron icon up; show elements for every page
            //     string += "<a class='accordion-toggle collapsed' data-toggle='collapse' data-parent='#accordionParent' href='#collapseElement"+ pageIndex+ "' style='outline:0;'>";
            //     string += "<h5 class='panel-title' style='font-weight:normal;'>Wikidata statements</h5>";
            //     string += "</a>";
            //     string += "</div>";
            //     // panel-collapse collapse: hide the content of statemes when starting the page; panel-collapse collapse in: show it
            //     string += "<div id='collapseElement"+ pageIndex +"' class='panel-collapse collapse'>";
            //     string += "<div class='panel-body'>";
            //     string += "<table class='statements' style='width:100%;background-color:#fff;border:1px'>" + localHtml + "</table>";
            //     string += "</div></div></div></div></p>";
            // }

            // reference of Wikipedia/Wikidata
            if ((wikipedia != null) || (wikidataId != null)) {
                string += '<p>References: '
                if (wikipedia != null) {
                    string += '<a href="http://' + lang + '.wikipedia.org/wiki?curid=' +
                        wikipedia +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;margin-top:5px;" ' +
                        ' src="resources/wikipedia.png"/></a>';
                }
                if (wikidataId != null) {
                    string += '<a href="https://www.wikidata.org/wiki/' +
                        wikidataId +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;margin-top:5px;" ' +
                        ' src="resources/Wikidata-logo.svg"/></a>';
                }
                string += '</p>';
            }

            string += "</div></div>";
        }
        $('#detailed_annot-' + pageIndex).html(string);
        $('#detailed_annot-' + pageIndex).show();
    }


    const wikimediaURL_prefix = 'https://';
    const wikimediaURL_suffix = '.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&pithumbsize=200&pageids=';

    var supportedLanguages = ["en", "es", "it", "fr", "de"];
    wikimediaUrls = {};
    for (var i = 0; i < supportedLanguages.length; i++) {
        var lang = supportedLanguages[i];
        wikimediaUrls[lang] = wikimediaURL_prefix + lang + wikimediaURL_suffix
    }

    var imgCache = {};
    window.lookupWikiMediaImage = function (wikipedia, lang) {
        // first look in the local cache
        if (lang + wikipedia in imgCache) {
            var imgUrl = imgCache[lang + wikipedia];
            var document = (window.content) ? window.content.document : window.document;
            var spanNode = document.getElementById("img-" + wikipedia);
            spanNode.innerHTML = '<img src="' + imgUrl + '"/>';
        } else {
            // otherwise call the wikipedia API
            var theUrl = wikimediaUrls[lang] + wikipedia;

            // note: we could maybe use the en cross-lingual correspondence for getting more images in case of
            // non-English pages
            $.ajax({
                url: theUrl,
                jsonp: "callback",
                dataType: "jsonp",
                xhrFields: {withCredentials: true},
                success: function (response) {
                    var document = (window.content) ? window.content.document : window.document;
                    var spanNode = document.getElementById("img-" + wikipedia);
                    if (response.query && spanNode) {
                        if (response.query.pages[wikipedia]) {
                            if (response.query.pages[wikipedia].thumbnail) {
                                var imgUrl = response.query.pages[wikipedia].thumbnail.source;
                                spanNode.innerHTML = '<img src="' + imgUrl + '"/>';
                                // add to local cache for next time
                                imgCache[lang + wikipedia] = imgUrl;
                            }
                        }
                    }
                }
            });
        }
    };

    function annotateCitations (page, coordinates, wikidataID, doi, page_width,  page_height){
        var pageDiv = $('#page-' + page);
        var canvas = pageDiv.children('canvas').eq(0);
        var canvasHeight = canvas.height()
        var canvasWidth = canvas.width()

        scale_x = canvasHeight / page_height ;
        scale_y = canvasWidth / page_width;
        var x = coordinates[0] * scale_x
        var y = coordinates[1] * scale_y
        var width = coordinates[2] * scale_x
        var height = coordinates[3] * scale_y;

        theId = "value";
        var element = document.createElement("a");
        var attributes = "display:block; width:" + width + "px; height:" + height + "px; position:absolute; top:" +
            y + "px; left:" + x + "px;";

        if(wikidataID !== null) {
            element.setAttribute("style", attributes + "border:2px solid; border-color: green");
            element.setAttribute("target", "_blank");
            element.setAttribute("href", "https://www.wikidata.org/wiki/" + wikidataID);
        } else if (doi !== null) {
            element.setAttribute("style", attributes + "border:2px solid; border-color: green");
            element.setAttribute("target", "_blank");
            element.setAttribute("href", "https://dx.doi.org/" + doi);
        } else {
            element.setAttribute("style", attributes + "border:2px solid; border-color: gray");
        }
        element.setAttribute("class", "citation");
        element.setAttribute("page", page);
        pageDiv.append(element);
    }

    function annotateAuthors(coordinates, author, page_width,  page_height){

        var page = coordinates[0]
        var pageDiv = $('#page-' + page);
        var canvas = pageDiv.children('canvas').eq(0);
        var canvasHeight = canvas.height()
        var canvasWidth = canvas.width()

        scale_x = canvasHeight / page_height ;
        scale_y = canvasWidth / page_width;
        var x = coordinates[1] * scale_x
        var y = coordinates[2] * scale_y
        var width = coordinates[3] * scale_x
        var height = coordinates[4] * scale_y
        theId = "value";
        var element = document.createElement("a");
        var attributes = "display:block; width:" + width + "px; height:" + height + "px; position:absolute; top:" +
            y + "px; left:" + x + "px;";
        element.setAttribute("style", attributes + "border:2px solid; border-color: red");
        element.setAttribute("class", theId);
        element.setAttribute("data-toggle", 'tooltip');
        element.setAttribute("data-placement", 'top');
        element.setAttribute("data-title", 'Confidence score: '+ author.confidence);
        element.setAttribute("id", 'annot-' + author.id);
        element.setAttribute("page", page);
        element.setAttribute("target", "_blank");
        element.setAttribute("href", "https://aurehal.archives-ouvertes.fr/author/read/id/" + author.id);
        pageDiv.append(element);

    }




    function viewQuantityPDF() {
        var pageIndex = $(this).attr('pdf');
        var localID = $(this).attr('id');

        console.log('viewQuanityPDF ' + pageIndex + ' / ' + localID);

        var ind1 = localID.indexOf('-');
        var ind2 = localID.indexOf('-', ind1 + 1);
        var localMeasurementNumber = parseInt(localID.substring(ind1 + 1, ind2));
        //var localMeasurementNumber = parseInt(localID.substring(ind1 + 1, localID.length));
        if ((measurementMap[localMeasurementNumber] == null) || (measurementMap[localMeasurementNumber].length == 0)) {
            // this should never be the case
            console.log("Error for visualising annotation measurement with id " + localMeasurementNumber
                + ", empty list of measurement");
        }

        var quantityMap = measurementMap[localMeasurementNumber];
        //console.log(quantityMap);
        var measurementType = null;
        var string = "";
        if (quantityMap.length == 1) {
            measurementType = "Atomic value";
            string = toHtml(quantityMap, measurementType, $(this).position().top);
        } else if (quantityMap.length == 2) {
            measurementType = "Interval";
            string = intervalToHtml(quantityMap, measurementType, $(this).position().top);
        } else {
            measurementType = "List";
            string = toHtml(quantityMap, measurementType, $(this).position().top);
        }
//console.log(string);
        $('#detailed_quantity-' + pageIndex).html(string);
        $('#detailed_quantity-' + pageIndex).show();
    }



});