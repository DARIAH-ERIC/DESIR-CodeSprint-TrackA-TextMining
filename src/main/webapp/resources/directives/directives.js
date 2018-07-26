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
                            var reader = new FileReader();
                            var loadingTask = scope.pdfjsLib.getDocument(window.URL.createObjectURL(element[0].files[0]));
                            loadingTask.promise.then(function(pdf) {
                                console.log('PDF loaded');

                                // Fetch the first page
                                var pageNumber = 1;
                                pdf.getPage(pageNumber).then(function(page) {
                                    console.log('Page loaded');

                                    var scale = 1.5;
                                    var viewport = page.getViewport(scale);

                                    // Prepare canvas using PDF page dimensions
                                    var canvas = document.getElementById('the-canvas');
                                    var context = canvas.getContext('2d');
                                    canvas.height = viewport.height;
                                    canvas.width = viewport.width;

                                    // Render PDF page into canvas context
                                    var renderContext = {
                                        canvasContext: context,
                                        viewport: viewport
                                    };
                                    var renderTask = page.render(renderContext);
                                    renderTask.then(function () {
                                        console.log('Page rendered');
                                    });
                                });
                            }, function (reason) {
                                // PDF loading error
                                console.error(reason);
                            });
                        });

                    });
                }
            };
        }]
    );

// angular.module("org.dariah.desir.ui")
//     .directive('pdfFile', )