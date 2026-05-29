def utils
def K8S_NS
pipeline {
    agent any
    
    environment {
        PROJECT_NAME="boutique"
        
        // Application SCM version
        VERSION = "v0.10.5" // Online Boutique release version

        //K8S Settings
        K8S_CONTROLLER_IP= "192.168.1.231"
        K8S_CONTROLLER_USER= "admin"
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
                    env.GIT_COMMIT_SHORT= "$GIT_COMMIT".take(7)
                    env.K8S_NS="$PROJECT_NAME-$VERSION".replace('.','-')

                    // Clear old builds
                    sh 'rm -rf microservices-demo manifestbook-* helm'

                    // ToDo: Don't forget to delete external repo before push back project repo
                    // ToDo: Don't forget to delete external repo before push back project repo
                    // ToDo: Don't forget to delete external repo before push back project repo

                    utils.createNewManifestBook()
                    utils.createNewHelmChart()
                }
            }
        }

        stage('Pulling Code') {
            steps {
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
                    def img = utils.cartserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
                }
            }
        }

        // stage("Frontend Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="frontend"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.frontendImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Product Catalog Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="productcatalogservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.productcatalogserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Currency Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="currencyservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.currencyserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Payment Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="paymentservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.paymentserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Shipping Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="shippingservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.shippingserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Email Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="emailservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.emailserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Checkout Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="checkoutservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.checkoutserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Recommendation Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="recommendationservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.recommendationserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Ad Service Work"){
        //     steps{
        //         script{
        //             def SERVICE_NAME="adservice"
        //             def SRC_DIR="microservices-demo/src/$SERVICE_NAME"
        //             utils.adserviceImageWork([serviceName: SERVICE_NAME, srcDir: SRC_DIR ])
        //         }
        //     }
        // }

        // stage("Create/Refresh K8S Deployment"){
        //     steps{
        //         sshagent(['mac_rsa_priv']) {
        //             script{
        //                 sh """
        //                     sed -i /${K8S_CONTROLLER_IP}/d ~/.ssh/known_hosts
        //                     ssh-keyscan -H ${K8S_CONTROLLER_IP} >> ~/.ssh/known_hosts
        //                     ssh ${K8S_CONTROLLER_USER}@${K8S_CONTROLLER_IP} 'kubectl delete -f manifestbook-${env.K8S_NS}.yml' 
        //                     scp manifestbook-${env.K8S_NS}.yml ${K8S_CONTROLLER_USER}@${K8S_CONTROLLER_IP}:~/
        //                     ssh ${K8S_CONTROLLER_USER}@${K8S_CONTROLLER_IP} 'podman image prune -f' 
        //                     ssh ${K8S_CONTROLLER_USER}@${K8S_CONTROLLER_IP} 'kubectl apply -f manifestbook-${env.K8S_NS}.yml' 
        //                 """

        //             }
        //         }
        //     }
        // }
    }
}
