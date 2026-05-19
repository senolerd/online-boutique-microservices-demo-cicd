// utils.groovy

def imageWork(Map imgInfo) {
    // Builds and uploads image to Artifactory


    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName:$VERSION"

    sh """ 
            # Solving some image naming problems that ocured at adservice    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "Card Service Image Creation"
        
            cd ${imgInfo.srcDir}
            


            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Dockerfile
            
            echo "Building $imgInfo.serviceName container"
            sed -i '/ARG BUILDPLATFORM=/d' Dockerfile
            podman build -t $IMAGE .
            
            echo "Login to Artifactory"
            podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE

        """
    def return_obj = [:]
    return_obj.name = $imgInfo.serviceName 
    return_obj.img = IMAGE 
    return_obj.port = 10
    
    

    return  return_obj
}

def deploymentTemplate(Map deplCfg){
    // Expected object for deplCfg [mame: , img: , port: ]


    return """ 
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