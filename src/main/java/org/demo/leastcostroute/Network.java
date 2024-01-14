package org.demo.leastcostroute;

public class Network {
    String sourceNetworkElement;
    String link;
    String destinationNetworkElement;


    public Network(String sourceNetworkElement, String link, String destinationNetworkElement) {
        this.sourceNetworkElement = sourceNetworkElement;
        this.link = link;
        this.destinationNetworkElement = destinationNetworkElement;
    }
}
