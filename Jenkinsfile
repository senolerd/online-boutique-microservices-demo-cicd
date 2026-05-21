def utils
pipeline {
    agent any

    environment {
        PROJECT_NAME="boutique"
        
        // Application SCM version
        VERSION = "v0.10.5" // Online Boutique release version

        //K8S Settings
        K8S_CONFIG= "kube_kvm_config" // Secret file name for k8s connection

        // Artifactory Settings
        // CONTAINER_REGISTRY="192.168.1.90:8081"
        CONTAINER_REGISTRY="docker.io"
        CONTAIER_REPO= "alkol" // it is username for Docker. Repo name for Nexus. This is the part of image name between registry address and image name
        REGISTRY_USE_TLS="true"

    }

    stages {
        stage("__init__") {
            steps{
                script{ 
                    utils = load 'libs/utils.groovy'
                    env.K8S_NS="$PROJECT_NAME-$VERSION".replace('.','-')
                    echo "${env.K8S_NS}"
                    utils.createNewManifestBook()
                }
            }
        }

        stage('Pulling Code') {
            steps {
                echo 'Hello from online boutique microservices demo'
                sh 'rm -rf microservices-demo'
                sh "git clone --branch release/$VERSION https://github.com/GoogleCloudPlatform/microservices-demo.git"         

                echo "Login to Artifactory"
                withCredentials([usernamePassword(credentialsId: 'docker-io-alkol', passwordVariable: 'PASS', usernameVariable: 'UNAME')]) {
                    echo "Login to Artifactory"
                    sh 'podman login --tls-verify=$REGISTRY_USE_TLS $CONTAINER_REGISTRY --username $UNAME --password $PASS'
                }
            }
        }

        stage("Cart Service Work"){
            // Cart service also needs redis service
            steps{
                script{
                    def SERVICE_NAME="cartservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME/src"
                    def img = utils.cartserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Frontend Service Work"){
            steps{
                script{
                    def SERVICE_NAME="frontend"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.frontendWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Product Catalog Service Work"){
            steps{
                script{
                    def SERVICE_NAME="productcatalogservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.productcatalogserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Currency Service Work"){
            steps{
                script{
                    def SERVICE_NAME="currencyservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.currencyserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Payment Service Work"){
            steps{
                script{
                    def SERVICE_NAME="paymentservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.paymentserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Shipping Service Work"){
            steps{
                script{
                    def SERVICE_NAME="shippingservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.shippingserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Email Service Work"){
            steps{
                script{
                    def SERVICE_NAME="emailservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.emailserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Checkout Service Work"){
            steps{
                script{
                    def SERVICE_NAME="checkoutservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.checkoutserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Recommendation Service Work"){
            steps{
                script{
                    def SERVICE_NAME="recommendationservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.recommendationserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        stage("Ad Service Work"){
            steps{
                script{
                    def SERVICE_NAME="adservice"
                    def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
                    utils.adserviceWorks([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }
    }
}



// image: 192.168.1.90:8081/devrepo/cartservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/frontend:v0.10.5
// image: 192.168.1.90:8081/devrepo/productcatalogservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/currencyservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/paymentservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/shippingservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/emailservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/checkoutservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/recommendationservice:v0.10.5
// image: 192.168.1.90:8081/devrepo/adservice:v0.10.5





// cartservice	            C#      Stores the items in the user's shopping cart in Redis and retrieves it.
// frontend	                Go	    Exposes an HTTP server to serve the website. Does not require signup/login and generates session IDs for all users automatically.
// productcatalogservice	Go      Provides the list of products from a JSON file and ability to search products and get individual products.
// currencyservice	        Node.js	Converts one money amount to another currency. Uses real values fetched from European Central Bank. It's the highest QPS service.
// paymentservice	        Node.js	Charges the given credit card info (mock) with the given amount and returns a transaction ID.
// shippingservice	        Go	    Gives shipping cost estimates based on the shopping cart. Ships items to the given address (mock)
// emailservice	            Python	Sends users an order confirmation email (mock).
// checkoutservice	        Go	    Retrieves user cart, prepares order and orchestrates the payment, shipping and the email notification.
// recommendationservice	Python	Recommends other products based on what's given in the cart.
// adservice	            Java	Provides text ads based on given context words.

// loadgenerator            Python/Locust	Continuously sends requests imitating realistic user shopping flows to the frontend.