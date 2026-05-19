// utils.groovy
def sayHello(name) {
    echo "Hello, ${name}!"
}

def imageWork(Map imgInfo) {
    echo "imgInfo.srcDir => $imgInfo.srcDir"
    echo "imgInfo.serviceName => $imgInfo.serviceName"


    def IMAGE="$CONTAINER_REGISTRY/$CONTAIER_REPO/$imgInfo.serviceName:$VERSION"

    sh """ 
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


