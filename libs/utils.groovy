// utils.groovy
// Admit 1: Creating files via Heredocs looks a little nasty when you have to hit 
// them to left wall. It should be solved ages ago, i will learn the best practise
// like pyyaml or or groovy {write|read}Yaml


def imageWorkFinisher(Map imgInfo, Map options) {
    // Builds image and uploads image to Artifactory and create manifests and chart
    // imgInfo = [serviceName: <string>, srcDir: <string> ]
    // options =    [ 
    //                manifest: [deployment: <boolean>, service: <boolean>], 
    //                chart: [deployment: <boolean>, service: <boolean>] 
    //              ] // whether the standart manifests and chart creators is going to be used

    _addServiceListToDockerfile(imgInfo)

    def PORT = sh(script:"grep EXPOSE $imgInfo.srcDir/Dockerfile", returnStdout: true).replace("EXPOSE ","").trim().toInteger()
    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName-$VERSION:$GIT_COMMIT_SHORT"

    sh """ 
            # Solving some image naming problems that ocured at some services    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "$imgInfo.serviceName Service Image Creation"       
            cd $imgInfo.srcDir
            
            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Dockerfile
            
            echo "Building $imgInfo.serviceName container"
            sed -i '/ARG BUILDPLATFORM=/d' Dockerfile
            podman build -t $IMAGE .

            #################################################
            # TODO 2: Trivy scanning should be here before uploading the image 
            #################################################


            ### ToDo: Add security check newly creted image
            ### Trivy
            #  podman run --rm \
            #  -v /run/user/1000/docker.sock:/var/run/docker.sock \
            #  -v $HOME/.cache:/root/.cache \
            #  aquasec/trivy:latest image \
            #  --severity HIGH,CRITICAL \
            #  --exit-code 1 \
            #  docker.io/alkol/currencyservice-v0.10.5:09ca5af


            echo "Login to Artifactory"
            podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE
            podman image prune -f
        """

    if ( options.manifest.deployment ) { _addDeploymentManifest([name: imgInfo.serviceName, img: IMAGE, port: PORT]) }
    if ( options.manifest.service ) { _addServiceManifest([name: imgInfo.serviceName, port: PORT]) }
    if ( options.chart.deployment ) { _addDeploymentToChart([name: imgInfo.serviceName, port: PORT, img: IMAGE]) }
    if ( options.chart.service ) { _addServiceToChart([name: imgInfo.serviceName, port: PORT]) }
    }


def currencyserviceImageWork(imgInfo) {
    // This api's Dockerfile needs a little more custom care.
    // Bug #1- Building image and deployment image of multistage source images are not matching and having conflict problem.
    // Bug #2- PORT environment variable is missing inside the container

    sh """ 
        cat << EOT > ${imgInfo.srcDir}/Dockerfile
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

    ENTRYPOINT [ "node", "server.js" ]        
    """
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}


def paymentserviceImageWork(imgInfo) {
    // This api's Dockerfile needs a little more custom care.
    // Bug #1- Building image and deployment image of multistage source images are not matching and having conflict problem.
    // Bug #2- PORT environment variable is missing inside the container

    sh """ 
        cat << EOT > ${imgInfo.srcDir}/Dockerfile
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
    # ENV DISABLE_PROFILER 1 
    # ENV DISABLE_TRACING 1
    # ENV DISABLE_DEBUGGER 1
    # ENV GCP_PROJECT "hello"
    # ENV GOOGLE_CLOUD_PROJECT "jello"
    ENTRYPOINT [ "node", "index.js" ]           
    """

    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def frontendImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def recommendationserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def shoppingassistantservice(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def checkoutserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def adserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def cartserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def emailserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def productcatalogserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}

def shippingserviceImageWork(imgInfo) {
    options = [manifest: [deployment: true, service: true], chart: [deployment: true, service: true] ]
    imageWorkFinisher(imgInfo, options)
}


def _addServiceListToDockerfile(imgInfo){
    // Appends environment variables for services to all Dockerfiles of API's
    echo "Adiong Services environment variables to Dockerfile"
    sh """
        cat << EOF >> ${imgInfo.srcDir}/Dockerfile
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

    """
    }


// ##################
// # Creating monolitic manifest file
// ##################

def createNewManifestBook(){
    // Creates a manifest book and adds namespace for whole deployment
    sh """
        echo "CREATING MANIFEST-BOOK WITH NAMESPACE FOR ${env.K8S_NS}" 
        echo "
---
apiVersion: v1
kind: Namespace
metadata:
    name: ${env.K8S_NS} " > manifestbook-${env.K8S_NS}-${GIT_COMMIT_SHORT}.yml
"""
}


def _addDeploymentManifest(Map deplCfg){
    // Expected object for deplCfg [mame:string , img:string , port: integer ] and returns deployment manifest for API
    sh """ 
    echo "ADDING DEPLOYMENT MANIFEST FOR ${deplCfg.name}"
    echo "
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
              - containerPort: ${deplCfg.port}" >> manifestbook-${env.K8S_NS}-${GIT_COMMIT_SHORT}.yml
    """
}



def _addServiceManifest(Map svcCfg){
    // Expected object for deplCfg [mame:string , port:integer ] and returns service manifest for API
    sh """
        echo "ADDING SERVICE MANIFEST FOR ${svcCfg.name}" 
        echo "
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
      targetPort: ${svcCfg.port}" >> manifestbook-${K8S_NS}-${GIT_COMMIT_SHORT}.yml
    """
}


// ####################################
// # HELMING
// ####################################

def createNewHelmChart(){
    sh """
        echo "Clear old helm chart dir"
        rm -rf ${HELM_CHART_PATH}
        echo "Create new helm chart directory layout"
        mkdir -p ${HELM_CHART_PATH}/charts ${HELM_CHART_PATH}/templates
        cd ${HELM_CHART_PATH}
        echo "Creating Chart.yaml"
        echo "
apiVersion: v2
name: test-helm
description: Boutique Demo Helm Chart @commit ${GIT_COMMIT_SHORT}
type: application
version: ${HELM_VERSION}
appVersion: ${VERSION} " > Chart.yaml

        echo "Creating values.yaml"
        echo '
commonEnvVars:
    AD_SERVICE_ADDR: "adservice:9555"
    CART_SERVICE_ADDR: "cartservice:7070"
    CHECKOUT_SERVICE_ADDR: "checkoutservice:5050"
    CURRENCY_SERVICE_ADDR: "currencyservice:7000"
    EMAIL_SERVICE_ADDR: "emailservice:8080"
    FRONTEND_SERVICE_ADDR: "frontend:8080"
    PAYMENT_SERVICE_ADDR: "paymentservice:50051"
    PRODUCT_CATALOG_SERVICE_ADDR: "productcatalogservice:3550"
    RECOMMENDATION_SERVICE_ADDR: "recommendationservice:8080"
    SHIPPING_SERVICE_ADDR: "shippingservice:50051"
    SHOPPING_ASSISTANT_SERVICE_ADDR: "shoppingassistantservice:8080"
    REDIS_ADDR: "redis-cart:6379"
    ENABLE_SHOPPING_ASSISTANT: false
    DISABLE_PROFILER: 1
    DISABLE_TRACING: 1
    DISABLE_DEBUGGER: 1
    GCP_PROJECT: "hello"
    GOOGLE_CLOUD_PROJECT: "jello" ' > values.yaml

    """
    _createConfigMap()
    _add_redis()
}


def _addDeploymentToChart(Map deplCfg){

    sh """ 
        echo "ADDING HELM CHART DEPLOYMENT FOR ${deplCfg.name}"
        echo "
apiVersion: apps/v1
kind: Deployment
metadata:
    name: {{ .Values.${deplCfg.name}.name }}
    namespace: {{ .Values.namespace }} 
    labels:
        app: {{ .Values.${deplCfg.name}.name }}
spec:
    replicas: 1
    selector:
        matchLabels:
            app: {{ .Values.${deplCfg.name}.name }}
    template:
        metadata:
            labels:
                app: {{ .Values.${deplCfg.name}.name }}
        spec:
            containers:
            - name: {{ .Values.${deplCfg.name}.name }}
              image: {{ .Values.${deplCfg.name}.image }}
              imagePullPolicy: Always
              ports:
              - containerPort: {{ .Values.${deplCfg.name}.port }}
              envFrom:
              - configMapRef:
                  name: common-env-vars"  > helm/templates/${deplCfg.name}.yaml
    """ 

    // Adding values to charts values.yaml
    sh """ 
    echo "ADDING HELM CHART SERVICE FOR ${deplCfg.name}"

    echo "
${deplCfg.name}:
    name: ${deplCfg.name}
    image: ${deplCfg.img}
    port: ${deplCfg.port} " >> helm/values.yaml
    """
}

def _addServiceToChart(Map svcCfg){

    sh """
        echo "ADDING SERVICE TO CHART ${svcCfg.name}" 
        echo "---
apiVersion: v1
kind: Service
metadata:
    name: {{ .Values.${svcCfg.name}.name }}
    namespace: {{ .Values.namespace }} 
spec:
    selector:
        app:  {{ .Values.${svcCfg.name}.name }}
    ports:
    - protocol: TCP
      port:  {{ .Values.${svcCfg.name}.port }}
      targetPort:  {{ .Values.${svcCfg.name}.port }} " >> helm/templates/${svcCfg.name}.yaml
    """
}

def _createConfigMap(){
    echo "Creating ConfigMap"
    sh '''
        echo '
apiVersion: v1
kind: ConfigMap
metadata:
    name: common-env-vars
data:
    {{- range $key, $value := .Values.commonEnvVars }}
    {{ $key }}: {{ $value | quote }}
    {{- end }} ' >> ${HELM_CHART_PATH}/templates/configmaps.yaml

    '''    
}

def _add_redis(){
    def redis_cfg = [
        name: "redis-cart",
        img: "redis",
        port: "6379"
    ]

    _addServiceManifest(redis_cfg)
    _addDeploymentManifest(redis_cfg)

    sh """
        echo "---
apiVersion: apps/v1
kind: Deployment
metadata:
    name: ${redis_cfg.name}
    namespace: {{ .Values.namespace }} 
    labels:
        app: ${redis_cfg.name}
spec:
    replicas: 1
    selector:
        matchLabels:
            app: ${redis_cfg.name}
    template:
        metadata:
            labels:
                app: ${redis_cfg.name}
        spec:
            containers:
            - name: ${redis_cfg.name}
              image: ${redis_cfg.img}
              imagePullPolicy: Always
              ports:
              - containerPort: ${redis_cfg.port} 
              envFrom:
              - configMapRef:
                  name: common-env-vars"  > helm/templates/${redis_cfg.name}.yaml


        echo "---
apiVersion: v1
kind: Service
metadata:
    name: ${redis_cfg.name}
    namespace: {{ .Values.namespace }} 
spec:
    selector:
        app:  ${redis_cfg.name}
    ports:
    - protocol: TCP
      port:  ${redis_cfg.port}
      targetPort: ${redis_cfg.port} " >> helm/templates/${redis_cfg.name}.yaml
    """
}

return this