angular.module('org.dariah.desir.ui').controller('uploadController', function ($scope, $http) {

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
                },
                function (e) {  //error
                    $scope.jsonResponse = undefined;
                    $scope.errorMessage = e;
                }
            );
    }
});