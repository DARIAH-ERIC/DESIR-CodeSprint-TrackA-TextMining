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

                            var pdfDoc = null,
                                pageNum = 1,
                                pageRendering = false,
                                pageNumPending = null,
                                scale = 1.5,
                                canvas = document.getElementById('the-canvas'),
                                ctx = canvas.getContext('2d');


                            /**
                             * Get page info from document, resize canvas accordingly, and render page.
                             * @param num Page number.
                             */
                            function renderPage(num) {
                                pageRendering = true;
                                // Using promise to fetch the page
                                pdfDoc.getPage(num).then(function(page) {
                                    var viewport = page.getViewport(scale);
                                    canvas.height = viewport.height;
                                    canvas.width = viewport.width;

                                    // Render PDF page into canvas context
                                    var renderContext = {
                                        canvasContext: ctx,
                                        viewport: viewport
                                    };
                                    var renderTask = page.render(renderContext);

                                    // Wait for rendering to finish
                                    renderTask.promise.then(function() {
                                        pageRendering = false;
                                        if (pageNumPending !== null) {
                                            // New page rendering is pending
                                            renderPage(pageNumPending);
                                            pageNumPending = null;
                                        }
                                    });
                                });

                                // Update page counters
                                document.getElementById('page_num').textContent = num;
                            }

                            /**
                             * If another page rendering in progress, waits until the rendering is
                             * finised. Otherwise, executes rendering immediately.
                             */
                            function queueRenderPage(num) {
                                if (pageRendering) {
                                    pageNumPending = num;
                                } else {
                                    renderPage(num);
                                }
                            }

                            /**
                             * Displays previous page.
                             */
                            function onPrevPage() {
                                if (pageNum <= 1) {
                                    return;
                                }
                                pageNum--;
                                queueRenderPage(pageNum);
                            }
                            document.getElementById('prev').addEventListener('click', onPrevPage);

                            /**
                             * Displays next page.
                             */
                            function onNextPage() {
                                if (pageNum >= pdfDoc.numPages) {
                                    return;
                                }
                                pageNum++;
                                queueRenderPage(pageNum);
                            }
                            document.getElementById('next').addEventListener('click', onNextPage);

                            /**
                             * Asynchronously downloads PDF.
                             */
                            var loadingTask = scope.pdfjsLib.getDocument(window.URL.createObjectURL(element[0].files[0]));
                            loadingTask.promise.then(function(pdf) {
                                pdfDoc = pdf;
                                console.log(pdfDoc)
                                document.getElementById('page_count').textContent = pdfDoc.numPages;
                                // Initial/first page rendering
                                renderPage(pageNum);
                            });



                        });

                    });
                }
            };
        }]
    );



