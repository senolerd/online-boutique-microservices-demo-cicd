// utils.groovy



def imageWorkFinisher(Map imgInfo) {
    // Builds and uploads image to Artifactory 

    _addServiceListToDockerfile(imgInfo)

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

            #################################################
            # TODO: Add a build number to end of the image to make easy roll-back or create a HELM chart, or do both. Yeah, do both!
            #################################################

            #                 podman build -t $IMAGE .

            #################################################
            # TODO 2: Trivy scanning should be here before uploading the image 
            #################################################


            echo "Login to Artifactory"
            #              podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE
            #              podman image prune -f
        """
            // Trivy Sample
            // # podman run --rm \
            // # -v /var/run/docker.sock:/var/run/docker.sock \
            // # -v $HOME/.cache:/root/.cache \
            // # aquasec/trivy:latest image \
            // # --severity HIGH,CRITICAL \
            // # --exit-code 1 \
            // # docker.io/alkol/currencyservice:v0.10.5

    _addDeploymentManifest([name: imgInfo.serviceName, img: IMAGE, port: PORT]) 
    _addServiceManifest([name: imgInfo.serviceName, port: PORT])
    
    }

/////// Special cares per API code


def currencyserviceWorks(imgInfo) {
    // This api's Dockerfile needs a little more custom care.
    // Bug #1- Building image and deployment image of multistage source images are not matching and having conflict problem.
    // Bug #2- PORT environment variable is missing inside the container
    sh """ 
        echo "\rFROM node:20.20.0-alpine AS builder
            \rRUN apk add --update --no-cache \
            \r    python3 \
            \r    make \
            \r    g++
            \rWORKDIR /usr/src/app
            \rCOPY package*.json ./
            \rRUN npm install --only=production
            \rFROM node:20.20.0-alpine
            \rRUN apk add --no-cache nodejs
            \rWORKDIR /usr/src/app
            \rCOPY --from=builder /usr/src/app/node_modules ./node_modules
            \rCOPY . .
            \rEXPOSE 7000
            \rENV PORT 7000
            \rENTRYPOINT [ "node", "server.js" ] " > ${imgInfo.srcDir}/Dockerfile"
    """

    imageWorkFinisher(imgInfo)
    }

def paymentserviceWorks(imgInfo) {
    // Error: Cannot find module '/usr/src/app/node_modules/pprof/build/node-v137-linux-x64-musl/pprof.node'
    imageWorkFinisher(imgInfo)
    }

def frontendWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def recommendationserviceWorks(imgInfo) {
    // raise Exception('PRODUCT_CATALOG_SERVICE_ADDR environment variable not set')
    imageWorkFinisher(imgInfo)
    }

def shoppingassistantservice(imgInfo) {
    // ModuleNotFoundError: No module named 'aiohttp'
    imageWorkFinisher(imgInfo)
    }

// WORKING APIS

def checkoutserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }
    
def adserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def cartserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def emailserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def productcatalogserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def shippingserviceWorks(imgInfo) {
    imageWorkFinisher(imgInfo)
    }

def _addServiceListToDockerfile(imgInfo){
    // Appends environment variables for services to all Dockerfiles of API's
    sh """
        echo "
        \rENV AD_SERVICE_ADDR service/adservice
        \rENV CART_SERVICE_ADDR service/cartservice
        \rENV CHECKOUT_SERVICE_ADDR service/checkoutservice
        \rENV CURRENCY_SERVICE_ADDR service/currencyservice
        \rENV EMAIL_SERVICE_ADDR service/emailservice
        \rENV FRONTEND_SERVICE_ADDR service/frontend
        \rENV PAYMENT_SERVICE_ADDR service/paymentservice
        \rENV PRODUCT_CATALOG_SERVICE_ADDR service/productcatalogservice
        \rENV RECOMMENDATION_SERVICE_ADDR service/recommendationservice
        \rENV SHIPPING_SERVICE_ADDR service/shippingservice
        \rENV SHOPPING_ASSISTANT_SERVICE_ADDR service/shoppingassistantservice" >> ${imgInfo.srcDir}/Dockerfile
    """
}


/////// Special cares per API code ENDS

def createNewManifestBook(){
    // Creates a manifest book and adds namespace for whole deployment
    
    echo "ADDING/CREATING NAMESPACE MANIFEST FOR ${svcCfg.name}"
    sh """
        echo "\r---
        \rapiVersion: v1
        \rkind: Namespace
        \rmetadata:
        \r    name: ${env.K8S_NS}"  >  manifestbook-${env.K8S_NS}.yml
    """
}


def _addDeploymentManifest(Map deplCfg){
    // Expected object for deplCfg [mame:string , img:string , port: integer ] and returns deployment manifest for API
    
    echo "ADDING DEPLOYMENT MANIFEST FOR ${svcCfg.name}"
    sh """ 
        echo "\r--- 
        \rapiVersion: apps/v1
        \rkind: Deployment
        \rmetadata:
        \r    name: $deplCfg.name
        \r    namespace: ${K8S_NS}
        \r    labels:
        \r        app: ${deplCfg.name}
        \rspec:
        \r    replicas: 1
        \r    selector:
        \r        matchLabels:
        \r            app: ${deplCfg.name}
        \r    template:
        \r        metadata:
        \r            labels:
        \r                app: ${deplCfg.name}
        \r        spec:
        \r            containers:
        \r            - name: ${deplCfg.name}
        \r              image: ${deplCfg.img}
        \r              imagePullPolicy: Always
        \r              ports:
        \r              - containerPort: ${deplCfg.port}"  >> manifestbook-${env.K8S_NS}.yml
    """
}


def _addServiceManifest(Map svcCfg){
    // Expected object for deplCfg [mame:string , port:integer ] and returns service manifest for API

    echo "ADDING SERVICE MANIFEST FOR ${svcCfg.name}"
    sh """
        echo "\r---
        \rapiVersion: v1
        \rkind: Service
        \rmetadata:
        \r    name: ${svcCfg.name}
        \r    namespace: ${K8S_NS}
        \rspec:
        \r    selector:
        \r        app.kubernetes.io/name: ${svcCfg.name}
        \r    ports:
        \r    - protocol: TCP
        \r      port: ${svcCfg.port}
        \r      targetPort: ${svcCfg.port}" >> manifestbook-${K8S_NS}.yml
    """
}

return this