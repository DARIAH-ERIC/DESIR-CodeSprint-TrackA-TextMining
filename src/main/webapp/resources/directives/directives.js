angular.module('org.dariah.desir.ui')
    .directive('entity', function () {
            return {
                restrict: 'E',
                templateUrl: 'resources/templates/view-entity.html'
            }
        }
    );

angular.module('org.dariah.desir.ui')
    .directive('fileModel', ['$parse', function ($parse) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var model = $parse(attrs.fileModel);
                    var modelSetter = model.assign;
                    element.bind('change', function () {
                        scope.$apply(function () {
                            modelSetter(scope, element[0].files[0]);
                            /**
                             * Asynchronously downloads PDF.
                             */
                            var loadingTask = scope.pdfjsLib.getDocument(window.URL.createObjectURL(element[0].files[0]));
                            var viewer = document.getElementById('pdf-viewer');
                            var scale = 1.5;
                            var thePdf ;
                            loadingTask.promise.then(function(pdf) {
                                thePdf = pdf;

                                for(page = 1; page <= pdf.numPages; page++) {

                                    var table = document.createElement("table");
                                    table.setAttribute('style', 'table-layout: fixed; width: 100%;')
                                    var tr = document.createElement("tr");
                                    var td1 = document.createElement("td");
                                    var td2 = document.createElement("td");

                                    tr.appendChild(td1);
                                    tr.appendChild(td2);
                                    table.appendChild(tr);

                                    var canvas = document.createElement("canvas");
                                    canvas.setAttribute("id", 'pdf-page-canvas-' + page);
                                    var div = document.createElement("div");

                                    // Set id attribute with page-#{pdf_page_number} format
                                    div.setAttribute("id", "page-" + (page ));

                                    // This will keep positions of child elements as per our needs, and add a light border
                                    div.setAttribute("style", "position: relative; ");
                                    div.appendChild(canvas);
                                    // Append div within div#container
                                    td1.setAttribute('style', 'width:70%;');
                                    td1.appendChild(div);

                                    var annot = document.createElement("div");
                                    annot.setAttribute('style', 'vertical-align:top;');
                                    annot.setAttribute('id', 'detailed_annot-' + (page));
                                    td2.setAttribute('style', 'vertical-align:top;width:30%;');
                                    td2.appendChild(annot);

                                    viewer.appendChild(table);
                                    renderPage(page,canvas )
                                }
                            }, function (reason) {
                                // PDF loading error
                                console.error(reason);
                            });
                            function renderPage(pageNumber, canvas) {
                                thePdf.getPage(pageNumber).then(function(page) {
                                    viewport = page.getViewport(scale);
                                    canvas.height = viewport.height;
                                    canvas.width = viewport.width;
                                    page.render({canvasContext: canvas.getContext('2d'), viewport: viewport});
                                });
                            }

                        });

                    });
                }
            };
        }]
    );



