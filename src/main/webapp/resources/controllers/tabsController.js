angular.module('org.dariah.desir.ui', ['ui.bootstrap']);

angular.module('org.dariah.desir.ui')
    .controller('tabsController', function ($scope) {
            $scope.tabs = [
                {title: 'Dynamic Title 1', content: 'Dynamic content 1'},
                {title: 'Dynamic Title 2', content: 'Dynamic content 2', disabled: true}
            ];

            $scope.model = {
                name: 'Tabs'
            };
            $scope.pdfjsLib = window['pdfjs-dist/build/pdf'];
        }
    );





