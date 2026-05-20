// utils.groovy



def imageWorkFinisher(Map imgInfo) {
    // Builds and uploads image to Artifactory 

    def PORT = sh(script:"grep EXPOSE $imgInfo.srcDir/Dockerfile", returnStdout: true).replace("EXPOSE ","").trim().toInteger()
    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName:$VERSION"

    sh """ 
            # Solving some image naming problems that ocured at adservice    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "$imgInfo.serviceName Service Image Creation"       
            cd $imgInfo.srcDir
            
            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Dockerfile
            
            echo "Building $imgInfo.serviceName container"
            sed -i '/ARG BUILDPLATFORM=/d' Dockerfile
            podman build -t $IMAGE .

            #################################################
            # TODO:  Add a build number to end of the image to make easy roll-back or create a HELM chart, or do both. Yeah, do both!
            #################################################



            # echo "Login to Artifactory"
            # podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE


        """
    _deploymentManifest([name: imgInfo.serviceName, img: IMAGE, port: PORT]) 
    _serviceManifest([name: imgInfo.serviceName, port: PORT])
    
}




/////// Special cares per API code
def checkoutserviceWorks(imgInfo) {
    //Fix #1: panic: environment variable "SHIPPING_SERVICE_ADDR" not set
    sh """
        cd $imgInfo.srcDir
        echo 'ENV SHIPPING_SERVICE_ADDR service/shippingservice' >> Dockerfile

    """
    imageWorkFinisher(imgInfo)
    }

def currencyserviceWorks(imgInfo) {
    // Error: Cannot find module '/usr/src/app/node_modules/pprof/build/node-v137-linux-x64-musl/pprof.node'
    imageWorkFinisher(imgInfo)
    }

def frontendWorks(imgInfo) {
    // panic: environment variable "PRODUCT_CATALOG_SERVICE_ADDR" not set
    imageWorkFinisher(imgInfo)
    }

def paymentserviceWorks(imgInfo) {
    // Error: Cannot find module '/usr/src/app/node_modules/pprof/build/node-v137-linux-x64-musl/pprof.node'
    imageWorkFinisher(imgInfo)
    }
def recommendationserviceWorks(imgInfo) {
    // raise Exception('PRODUCT_CATALOG_SERVICE_ADDR environment variable not set')
    imageWorkFinisher(imgInfo)
    }

// WORKING APIS

def adserviceWorks(imgInfo) {imageWorkFinisher(imgInfo)}
def cartserviceWorks(imgInfo) {imageWorkFinisher(imgInfo)}
def emailserviceWorks(imgInfo) {imageWorkFinisher(imgInfo)}
def productcatalogserviceWorks(imgInfo) {imageWorkFinisher(imgInfo)}
def shippingserviceWorks(imgInfo) {imageWorkFinisher(imgInfo)}


/////// Special cares per API code ENDS




































def createNewManifestBook(){
    // Creates a manifest book and adds namespace for whole deployment
    sh """ cat <<-'END' >  manifestbook-${env.K8S_NS}.yml
---
apiVersion: v1
kind: Namespace
metadata:
    name: ${env.K8S_NS}
END
    """.stripIndent().trim()
}


def _deploymentManifest(Map deplCfg){
    // Expected object for deplCfg [mame:string , img:string , port: integer ] and returns deployment manifest for API
    echo "DEPLOYMENT MANIFEST IS CREATING"
    sh """ cat << END >> manifestbook-${env.K8S_NS}.yml
--- 
apiVersion: apps/v1
kind: Deployment
metadata:
    name: $deplCfg.name
    namespace: ${env.K8S_NS}
    labels:
        app: $deplCfg.name
spec:
    replicas: 1
    selector:
        matchLabels:
            app: $deplCfg.name
    template:
        metadata:
            labels:
                app: $deplCfg.name
        spec:
            containers:
            - name: $deplCfg.name
              image: $deplCfg.img
              ports:
              - containerPort: $deplCfg.port
END
    """.stripIndent()
}


def _serviceManifest(Map svcCfg){
    // Expected object for deplCfg [mame:string , port:integer ] and returns service manifest for API

    sh """
        cat << EOF >> manifestbook-${env.K8S_NS}.yml
---
apiVersion: v1
kind: Service
metadata:
    name: $svcCfg.name
    namespace: ${env.K8S_NS}
spec:
    selector:
        app.kubernetes.io/name: $svcCfg.name
    ports:
    - protocol: TCP
      port: $svcCfg.port
      targetPort: $svcCfg.port
EOF
    """
}

return this