// utils.groovy

def imageWork(Map imgInfo) {
    // Builds and uploads image to Artifactory

    def PORT = sh(script:"grep EXPOSE $imgInfo.srcDir/Dockerfile", returnStdout: true).replace("EXPOSE ","").trim().toInteger()
    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName:$VERSION"

    sh """ 
            # Solving some image naming problems that ocured at adservice    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "$imgInfo.serviceName Service Image Creation"       
            echo $imgInfo.srcDir
            
            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Dockerfile
            
            # echo "Building $imgInfo.serviceName container"
            # sed -i '/ARG BUILDPLATFORM=/d' Dockerfile
            # podman build -t $IMAGE .
            
            # echo "Login to Artifactory"
            # podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE

        """
    return  [name: imgInfo.serviceName, img: IMAGE, port: PORT]
}

def deploymentManifest(Map deplCfg){
    // Expected object for deplCfg [mame: , img: , port: ]

    echo "Manifest is based on: $deplCfg"

    def manifest =  """ 
apiVersion: apps/v1
kind: Deployment
metadata:
name: $deplCfg.name
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
---
    """
    echo manifest

}



return this


// apiVersion: apps/v1
// kind: Deployment
// metadata:
//   name: nginx-deployment
//   labels:
//     app: nginx
// spec:
//   replicas: 3
//   selector:
//     matchLabels:
//       app: nginx
//   template:
//     metadata:
//       labels:
//         app: nginx
//     spec:
//       containers:
//       - name: nginx
//         image: nginx:1.14.2
//         ports:
//         - containerPort: 80
//---
// apiVersion: v1
// kind: Service
// metadata:
//   name: my-service
// spec:
//   selector:
//     app.kubernetes.io/name: MyApp
//   ports:
//     - protocol: TCP
//       port: 80
//       targetPort: 9376