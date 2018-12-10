angular.module('org.dariah.desir.ui').controller('uploadController', function ($scope, $http) {
    var measurementMap = new Array();
    $scope.jsonResponse = null;
    $scope.processingAuthors = 'Authors';
    $scope.processingCitations = 'Citations';
    $scope.processingEntities = 'Named entities';
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
                urlPath = 'http://nerd.huma-num.fr/nerd/service/disambiguate'; // Nerd API
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


    function setupAnnotations(response, type) {
        var json = response ;
        var page_height = response.pageDimention.height;
        var page_width = response.pageDimention.width;;
        var result = response.results;
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
            // hey bro, this must be asynchronous to avoid blocking the brother ;)
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
        }
    }

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