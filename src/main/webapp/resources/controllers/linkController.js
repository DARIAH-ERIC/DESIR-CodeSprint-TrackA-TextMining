angular.module('org.dariah.desir.ui')
    .controller('linkController', function ($scope, $http) {
            $scope.rewrite = function (id) {
                console.log("Rewrite using the id " + id);
                var request = {
                    method: 'GET',
                    url: 'https://api.archives-ouvertes.fr/ref/author?wt=json&fl=docid&fl=valid_s&q=idHal_s:' + id,
                    headers: {
                        'Content-Type': undefined
                    }
                };

                $http(request)
                    .then(
                        function (d) {  //success
                            jsonData = d.data.response;
                            var numericId = "";
                            var docs = jsonData.docs;
                            for (var idx in docs) {
                                if (docs[idx].valid_s === "VALID") {
                                    numericId = docs[idx].docid;
                                    break;
                                } else {
                                    numericId = docs[idx].docid;
                                }
                            }
                            if (numericId !== "") {
                                // window.location.href = "https://aurehal.archives-ouvertes.fr/author/read/id/" + numericId;

                                window.open("https://aurehal.archives-ouvertes.fr/author/read/id/" + numericId, "_target")
                            }
                        },
                        function (e) {  //error
                            $scope.errorMessage = e;
                        }
                    );

            }

        }
    );
