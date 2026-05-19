// utils.groovy

def imageWork(Map imgInfo) {
    // Builds and uploads image to Artifactory


    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName:$VERSION"

    sh """ 
            # Solving some image naming problems that ocured at adservice    
            export CONTAINERS_SHORT_NAME_ALIASING=on

            echo "Card Service Image Creation"
        
            cd $imgInfo.srcDir
            
            # Podman doesn't need BUILDPLATROM, adds itself on the compile time and hates 
            # if there is defination in the Dockerfile
            
            echo "Building $imgInfo.serviceName container"
            sed -i '/ARG BUILDPLATFORM=/d' Dockerfile
            podman build -t $IMAGE .
            
            echo "Login to Artifactory"
            podman push --tls-verify=$REGISTRY_USE_TLS $IMAGE
        """
    
}

return this


