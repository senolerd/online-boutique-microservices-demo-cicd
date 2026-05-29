// utils.groovy



def imageWorkFinisher(Map imgInfo) {
    // Builds and uploads image to Artifactory 

    _addServiceListToContainerfile(imgInfo)

    def PORT = sh(script:"grep EXPOSE $imgInfo.srcDir/Containerfile", returnStdout: true).replace("EXPOSE ","").trim().toInteger()
    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName-$VERSION:$GIT_COMMIT_SHORT"

    sh """ 
            # Solving some image naming problems that ocured at adservice    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "$imgInfo.serviceName Service Image Creation"       
            cd $imgInfo.srcDir
            
            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Containerfile
            
            echo "Building $imgInfo.serviceName container"
            sed -i '/ARG BUILDPLATFORM=/d' Containerfile

            #################################################
            # TODO: Add a build number to end of the image to make easy roll-back or create a HELM chart, or do both. Yeah, do both!
            #################################################

            podman build -t $IMAGE .

            #################################################
            # TODO 2: Trivy scanning should be here before uploading the image 
            #################################################


            echo "Login to Artifactory"
            podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE
            podman image prune -f
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


def currencyserviceContainerfile(imgInfo) {
    // This api's Containerfile needs a little more custom care.
    // Bug #1- Building image and deployment image of multistage source images are not matching and having conflict problem.
    // Bug #2- PORT environment variable is missing inside the container

    sh """ 
        cat << EOT > ${imgInfo.srcDir}/Containerfile
    FROM node:20.20.0-alpine AS builder
    RUN apk add --update --no-cache  python3 make g++
    WORKDIR /usr/src/app
    COPY package*.json ./
    RUN npm install --only=production
    FROM node:20.20.0-alpine
    RUN apk add --no-cache nodejs
    WORKDIR /usr/src/app
    COPY --from=builder /usr/src/app/node_modules ./node_modules
    COPY . .
    EXPOSE 7000
    ENV PORT 7000
    ENV DISABLE_PROFILER 1 
    ENV DISABLE_TRACING 1
    ENV DISABLE_DEBUGGER 1
    ENV GCP_PROJECT "hello"
    ENV GOOGLE_CLOUD_PROJECT "jello"
    ENTRYPOINT [ "node", "server.js" ]        
    """
    imageWorkFinisher(imgInfo)
    }


def paymentserviceContainerfile(imgInfo) {
    // This api's Containerfile needs a little more custom care.
    // Bug #1- Building image and deployment image of multistage source images are not matching and having conflict problem.
    // Bug #2- PORT environment variable is missing inside the container

    sh """ 
        cat << EOT > ${imgInfo.srcDir}/Containerfile
    FROM node:20.20.0-alpine AS builder
    RUN apk add --update --no-cache  python3 make g++
    WORKDIR /usr/src/app
    COPY package*.json ./
    RUN npm install --only=production
    FROM node:20.20.0-alpine
    RUN apk add --no-cache nodejs
    WORKDIR /usr/src/app
    COPY --from=builder /usr/src/app/node_modules ./node_modules
    COPY . .
    EXPOSE 50051
    ENV PORT 50051
    ENV DISABLE_PROFILER 1 
    ENV DISABLE_TRACING 1
    ENV DISABLE_DEBUGGER 1
    ENV GCP_PROJECT "hello"
    ENV GOOGLE_CLOUD_PROJECT "jello"
    ENTRYPOINT [ "node", "index.js" ]           
    """
    imageWorkFinisher(imgInfo)
    }


def frontendContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def recommendationserviceContainerfile(imgInfo) {
    // raise Exception('PRODUCT_CATALOG_SERVICE_ADDR environment variable not set')
    imageWorkFinisher(imgInfo)
    }


def shoppingassistantservice(imgInfo) {
    // ModuleNotFoundError: No module named 'aiohttp'
    imageWorkFinisher(imgInfo)
    }


def checkoutserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def adserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def cartserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def emailserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def productcatalogserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def shippingserviceContainerfile(imgInfo) {
    imageWorkFinisher(imgInfo)
    }


def _addServiceListToContainerfile(imgInfo){
    // Appends environment variables for services to all Containerfiles of API's
    echo "Addong Services environment variables to Containerfile"
    sh """
        cat << 'EOF' >> ${imgInfo.srcDir}/Containerfile
    ENV AD_SERVICE_ADDR adservice:9555
    ENV CART_SERVICE_ADDR cartservice:7070
    ENV CHECKOUT_SERVICE_ADDR checkoutservice:5050
    ENV CURRENCY_SERVICE_ADDR currencyservice:7000
    ENV EMAIL_SERVICE_ADDR emailservice:8080
    ENV FRONTEND_SERVICE_ADDR frontend:8080
    ENV PAYMENT_SERVICE_ADDR paymentservice:50051
    ENV PRODUCT_CATALOG_SERVICE_ADDR productcatalogservice:3550
    ENV RECOMMENDATION_SERVICE_ADDR recommendationservice:8080
    ENV SHIPPING_SERVICE_ADDR shippingservice:50051
    ENV SHOPPING_ASSISTANT_SERVICE_ADDR shoppingassistantservice:8080
    ENV ENABLE_SHOPPING_ASSISTANT false
EOF    
    """
}

def createNewManifestBook(){
    // Creates a manifest book and adds namespace for whole deployment

    sh """
        echo "CREATING MANIFEST-BOOK WITH NAMESPACE FOR ${env.K8S_NS}" 
        cat << EOT >  manifestbook-${env.K8S_NS}.yml
---
apiVersion: v1
kind: Namespace
metadata:
    name: ${env.K8S_NS}
EOT
"""
}


def _addDeploymentManifest(Map deplCfg){
    // Expected object for deplCfg [mame:string , img:string , port: integer ] and returns deployment manifest for API

    sh """ 
        echo "ADDING DEPLOYMENT MANIFEST FOR ${deplCfg.name}"

    cat << EOT >> manifestbook-${env.K8S_NS}-${GIT_COMMIT_SHORT}.yml
--- 
apiVersion: apps/v1
kind: Deployment
metadata:
    name: ${deplCfg.name}
    namespace: ${K8S_NS}
    labels:
        app: ${deplCfg.name}
spec:
    replicas: 1
    selector:
        matchLabels:
            app: ${deplCfg.name}
    template:
        metadata:
            labels:
                app: ${deplCfg.name}
        spec:
            containers:
            - name: ${deplCfg.name}
              image: ${deplCfg.img}
              imagePullPolicy: Always
              ports:
              - containerPort: ${deplCfg.port}
"""
}


def _addServiceManifest(Map svcCfg){
    // Expected object for deplCfg [mame:string , port:integer ] and returns service manifest for API

    sh """
        echo "ADDING SERVICE MANIFEST FOR ${svcCfg.name}" 
    cat << EOT >> manifestbook-${K8S_NS}-${GIT_COMMIT_SHORT}.yml
---
apiVersion: v1
kind: Service
metadata:
    name: ${svcCfg.name}
    namespace: ${K8S_NS}
spec:
    selector:
        app: ${svcCfg.name}
    ports:
    - protocol: TCP
      port: ${svcCfg.port}
      targetPort: ${svcCfg.port}
"""
}

return this