angular.module('org.dariah.desir.ui').controller('uploadController', function ($scope, $http) {
    var measurementMap = new Array();

    $scope.uploadFile = function () {
        $scope.pdfLoaded = true;
        console.log($scope.pdfLoaded)
        $scope.jsonResponse = "";
        $scope.quantity = 4;
        
        var formData = new FormData();
        $scope.responseJson = "";
        formData.append("file", $scope.myFile);

        var request = {
            method: 'POST',
            url: window.location.href + '/process',
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
                    setupAnnotations($scope.jsonResponse)
                },
                function (e) {  //error
                    $scope.jsonResponse = undefined;
                    $scope.errorMessage = e;
                }
            );
    }

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
            entities.forEach(function (entity, n) {
                var entityDomain = entity.domains;
                measurementMap[n] = entity;
                //var theId = entity.type;
                var theUrl = null;
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
        $('a[page != "1"]').hide();
        pageDiv.height(canvasHeight);
        pageDiv.width(canvasWidth);
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